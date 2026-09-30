plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.vanniktech.mavenPublish)
}

android {
  namespace = "io.github.expo.peek.notification"
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
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  api(project(":peek-glance"))
  api(libs.androidx.core.ktx)

  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.robolectric)
}
