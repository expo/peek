import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
  id("io.github.expo.peek.glance-fork")
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.vanniktech.mavenPublish) apply false
}

val peekVersion: String = libs.versions.peek.get()

subprojects {
  group = "io.github.expo.peek"
  version = peekVersion
}

subprojects {
  plugins.withId("com.vanniktech.maven.publish") {
    extensions.configure<MavenPublishBaseExtension> {
      configure(AndroidSingleVariantLibrary(
        variant = "release",
        sourcesJar = true,
        publishJavadocJar = false,
      ))

      publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

      // Only sign when signing credentials are available (CI environment).
      if (project.findProperty("signingInMemoryKey") != null) {
        signAllPublications()
      }

      pom {
        name = project.name
        description = "Peek extends Jetpack Glance with notification composition."
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
  }
}
