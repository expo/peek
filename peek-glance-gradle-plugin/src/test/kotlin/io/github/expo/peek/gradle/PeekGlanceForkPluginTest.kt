package io.github.expo.peek.gradle

import java.io.File
import java.util.jar.JarOutputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class PeekGlanceForkPluginTest {
  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @Test
  fun substitutesDirectOfficialAppWidgetDependency() {
    val projectDir = createProject()
    publishModule(
      group = PeekGlanceForkCoordinates.forkGroup,
      module = PeekGlanceForkCoordinates.forkModule,
      version = PeekGlanceForkCoordinates.forkVersion,
    )
    writeBuild(
      projectDir,
      "implementation(\"${PeekGlanceForkCoordinates.officialAppWidgetModule}:" +
        "${PeekGlanceForkCoordinates.supportedGlanceVersion}\")",
    )

    val result = runner(projectDir).withArguments("resolveRuntimeClasspath").build()

    check(result.task(":resolveRuntimeClasspath")?.outcome == TaskOutcome.SUCCESS)
    assertContains(result.output, PeekGlanceForkCoordinates.forkCoordinate)
    assertFalse(
      result.output.contains(
        "${PeekGlanceForkCoordinates.officialAppWidgetModule}:" +
          PeekGlanceForkCoordinates.supportedGlanceVersion
      )
    )
  }

  @Test
  fun substitutesTransitiveAppWidgetDependency() {
    val projectDir = createProject()
    publishModule(
      group = PeekGlanceForkCoordinates.forkGroup,
      module = PeekGlanceForkCoordinates.forkModule,
      version = PeekGlanceForkCoordinates.forkVersion,
    )
    publishModule(
      group = PeekGlanceForkCoordinates.officialGroup,
      module = "glance-appwidget-multiprocess",
      version = PeekGlanceForkCoordinates.supportedGlanceVersion,
      dependencies = listOf(
        Triple(
          PeekGlanceForkCoordinates.officialGroup,
          PeekGlanceForkCoordinates.appWidgetModule,
          PeekGlanceForkCoordinates.supportedGlanceVersion,
        )
      ),
    )
    writeBuild(
      projectDir,
      "implementation(\"${PeekGlanceForkCoordinates.officialGroup}:" +
        "glance-appwidget-multiprocess:${PeekGlanceForkCoordinates.supportedGlanceVersion}\")",
    )

    val result = runner(projectDir).withArguments("resolveRuntimeClasspath").build()

    assertContains(result.output, PeekGlanceForkCoordinates.forkCoordinate)
    assertContains(
      result.output,
      "${PeekGlanceForkCoordinates.officialGroup}:glance-appwidget-multiprocess:" +
        PeekGlanceForkCoordinates.supportedGlanceVersion,
    )
  }

  @Test
  fun rejectsMismatchedGlanceVersion() {
    val projectDir = createProject()
    writeBuild(
      projectDir,
      "implementation(\"${PeekGlanceForkCoordinates.officialGroup}:glance:9.9.9\")",
    )

    val result =
      runner(projectDir)
        .withArguments("resolveRuntimeClasspath", "--stacktrace")
        .buildAndFail()

    assertContains(
      result.output,
      "Peek requires every androidx.glance module to use " +
        PeekGlanceForkCoordinates.supportedGlanceVersion,
    )
    assertContains(result.output, "androidx.glance:glance:9.9.9")
  }

  private fun createProject(): File {
    val projectDir = temporaryFolder.newFolder("consumer")
    File(projectDir, "settings.gradle.kts").writeText(
      """
      pluginManagement {
        repositories {
          gradlePluginPortal()
          mavenCentral()
        }
      }

      dependencyResolutionManagement {
        repositories {
          maven { url = uri(${quote(repositoryDir.absolutePath)}) }
        }
      }

      rootProject.name = "consumer"
      """.trimIndent()
    )
    return projectDir
  }

  private fun writeBuild(projectDir: File, dependency: String) {
    File(projectDir, "build.gradle.kts").writeText(
      """
      plugins {
        java
        id("io.github.expo.peek.glance-fork")
      }

      dependencies {
        $dependency
      }

      tasks.register("resolveRuntimeClasspath") {
        doLast {
          val runtimeClasspath = configurations.runtimeClasspath.get()
          runtimeClasspath.resolve()
          val components = runtimeClasspath.incoming.resolutionResult.allComponents
            .map { it.id.displayName }
            .sorted()
          println("RESOLVED=" + components.joinToString("|"))
        }
      }
      """.trimIndent().trimStart()
    )
  }

  private fun publishModule(
    group: String,
    module: String,
    version: String,
    dependencies: List<Triple<String, String, String>> = emptyList(),
  ) {
    val moduleDirectory =
      repositoryDir.resolve(group.replace('.', '/')).resolve(module).resolve(version)
    moduleDirectory.mkdirs()

    File(moduleDirectory, "$module-$version.pom").writeText(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project xmlns="http://maven.apache.org/POM/4.0.0">
        <modelVersion>4.0.0</modelVersion>
        <groupId>$group</groupId>
        <artifactId>$module</artifactId>
        <version>$version</version>
        <dependencies>
          ${dependencies.joinToString("\n") { (dependencyGroup, dependencyModule, dependencyVersion) ->
            """
            <dependency>
              <groupId>$dependencyGroup</groupId>
              <artifactId>$dependencyModule</artifactId>
              <version>$dependencyVersion</version>
            </dependency>
            """.trimIndent()
          }}
        </dependencies>
      </project>
      """.trimIndent().trimStart()
    )
    JarOutputStream(File(moduleDirectory, "$module-$version.jar").outputStream()).use { }
  }

  private fun runner(projectDir: File): GradleRunner =
    GradleRunner.create()
      .withProjectDir(projectDir)
      .withPluginClasspath()
      .forwardOutput()

  private fun quote(value: String): String = "\"${value.replace("\\", "\\\\")}\""

  private val repositoryDir: File
    get() = temporaryFolder.root.resolve("repository")
}
