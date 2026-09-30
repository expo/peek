plugins {
    id("com.android.library") version "8.12.0"
    id("org.jetbrains.kotlin.android") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    id("io.github.expo.peek.glance-fork") version "0.3.2"
}

android {
    namespace = "io.github.expo.peek.integration.standalone"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
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
    implementation("io.github.expo.peek:peek-notification:0.3.2")
    implementation("androidx.glance:glance-appwidget-multiprocess:1.2.0")
}

tasks.register("verifyStandaloneResolution") {
    doLast {
        val runtimeClasspath = configurations.getByName("debugRuntimeClasspath")
        runtimeClasspath.resolve()

        val componentIds = runtimeClasspath
            .incoming
            .resolutionResult
            .allComponents
            .map { it.id.displayName }

        val expectedFork =
            "io.github.expo.peek.forks:glance-appwidget:1.2.0-peek-0.3.2"
        check(expectedFork in componentIds) {
            "The Peek Glance fork was not selected: $componentIds"
        }
        check("androidx.glance:glance-appwidget:1.2.0" !in componentIds) {
            "The official appwidget artifact was not substituted: $componentIds"
        }
    }
}
