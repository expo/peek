package io.github.expo.peek.gradle

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySubstitution
import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.artifacts.component.ModuleComponentSelector

public class PeekGlanceForkPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val rootProject = project.rootProject
    val extraProperties = rootProject.extensions.extraProperties
    if (extraProperties.has(APPLIED_MARKER)) return
    extraProperties.set(APPLIED_MARKER, true)

    rootProject.allprojects.forEach { target ->
      target.dependencies.components.withModule(
        PeekGlanceForkCoordinates.forkGroupAndModule,
        Action<ComponentMetadataDetails> {
          allVariants {
            withCapabilities {
              addCapability(
                PeekGlanceForkCoordinates.officialGroup,
                PeekGlanceForkCoordinates.appWidgetModule,
                PeekGlanceForkCoordinates.supportedGlanceVersion,
              )
            }
          }
        },
      )

      target.configurations.configureEach(Action<Configuration> {
        if (state == Configuration.State.UNRESOLVED) {
          resolutionStrategy.dependencySubstitution(
            Action<DependencySubstitutions> {
              all(Action<DependencySubstitution> {
                val requestedModule = requested as? ModuleComponentSelector
                if (
                  requestedModule != null &&
                    requestedModule.group == PeekGlanceForkCoordinates.officialGroup
                ) {
                  val requestedVersion = requestedModule.version
                  if (!requestedVersion.isSupportedGlanceVersion()) {
                    throw GradleException(
                      "Peek requires every androidx.glance module to use " +
                        "${PeekGlanceForkCoordinates.supportedGlanceVersion}, but " +
                        "${requestedModule.group}:${requestedModule.module}:$requestedVersion " +
                        "was requested."
                    )
                  }

                  if (requestedModule.module == PeekGlanceForkCoordinates.appWidgetModule) {
                    useTarget(
                      PeekGlanceForkCoordinates.forkCoordinate,
                      "Peek custom emittables require the Glance translation extension hooks.",
                    )
                  }
                }
              })
            }
          )
        }
      })
    }
  }

  private fun String.isSupportedGlanceVersion(): Boolean =
    this == PeekGlanceForkCoordinates.supportedGlanceVersion ||
      this == "[${PeekGlanceForkCoordinates.supportedGlanceVersion}]"

  private companion object {
    const val APPLIED_MARKER = "io.github.expo.peek.glance-fork.applied"
  }
}
