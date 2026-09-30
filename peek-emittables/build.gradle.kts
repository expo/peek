plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.vanniktech.mavenPublish)
}

android {
  namespace = "io.github.expo.peek.emittables"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }

  lint {
    // Direct tree producers deliberately operate on Glance's restricted emittable model.
    disable += "RestrictedApi"
  }

  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  api(project(":peek-glance"))
  api(project(":peek-notification"))
  api(libs.androidx.glance.appwidget.multiprocess)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.robolectric)
}
