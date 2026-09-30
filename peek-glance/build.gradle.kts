plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.vanniktech.mavenPublish)
}

android {
  namespace = "io.github.expo.peek.glance"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }

  lint {
    // This module is the intentional implementation boundary for forked Glance translator
    // internals. Application-facing APIs remain ordinary public Glance APIs.
    disable += "RestrictedApi"
  }
}

dependencies {
  api(libs.androidx.glance.appwidget)
  implementation(libs.androidx.core.ktx)

  testImplementation(libs.androidx.test.core)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}
