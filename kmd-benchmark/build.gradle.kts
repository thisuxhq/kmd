plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.thisux.kmd.benchmark"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(project(":kmd-core"))
    testImplementation(libs.junit)
}

tasks.withType<Test>().configureEach {
    // Timings are reported, not asserted, so always rerun.
    outputs.upToDateWhen { false }
    testLogging.showStandardStreams = true
}
