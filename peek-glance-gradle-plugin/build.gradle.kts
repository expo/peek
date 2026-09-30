import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.vanniktech.mavenPublish)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

group = "io.github.expo.peek"
version = libs.versions.peek.get()

repositories {
  mavenCentral()
}

gradlePlugin {
  plugins {
    create("peekGlanceFork") {
      id = "io.github.expo.peek.glance-fork"
      implementationClass = "io.github.expo.peek.gradle.PeekGlanceForkPlugin"
      displayName = "Peek Glance fork resolver"
      description = "Selects and validates the Glance AppWidget fork required by Peek extensions."
    }
  }
}

mavenPublishing {
  configure(
    GradlePlugin(
      javadocJar = JavadocJar.Empty(),
      sourcesJar = true,
    )
  )

  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

  if (project.findProperty("signingInMemoryKey") != null) {
    signAllPublications()
  }

  pom {
    name = "Peek Glance fork resolver"
    description = "Selects the Peek AppWidget fork for supported AndroidX Glance dependencies."
    inceptionYear = "2026"
    url = "https://github.com/expo/peek"
    licenses {
      license {
        name = "The MIT License"
        url = "https://opensource.org/license/mit"
        distribution = "https://opensource.org/license/mit"
      }
    }
    developers {
      developer {
        id = "expo"
        name = "Expo"
        url = "https://github.com/expo"
      }
    }
    scm {
      url = "https://github.com/expo/peek"
      connection = "scm:git:git://github.com/expo/peek.git"
      developerConnection = "scm:git:ssh://git@github.com/expo/peek.git"
    }
  }
}

dependencies {
  testImplementation(kotlin("test-junit"))
  testImplementation("junit:junit:4.13.2")
}

tasks.test {
  useJUnit()
}
