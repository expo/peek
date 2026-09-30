plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "io.github.expo.peek.sample"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "io.github.expo.peek.sample"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = 1
    versionName = "0.1.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  implementation(project(":peek-glance"))
  implementation(project(":peek-notification"))
  implementation(libs.androidx.glance.appwidget.multiprocess)
  implementation(libs.androidx.core.ktx)
  implementation(libs.kotlinx.coroutines.android)
}
