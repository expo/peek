pluginManagement {
  includeBuild("peek-glance-gradle-plugin")

  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenLocal {
      content {
        includeGroup("io.github.expo.peek.forks")
      }
    }
    google()
    mavenCentral()
  }
}

rootProject.name = "peek"

include(
  ":peek-glance",
  ":peek-notification",
  ":peek-emittables",
  ":sample",
)
