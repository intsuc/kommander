plugins {
    alias(libs.plugins.multiplatform)
}

group = "dev.intsuc"
version = "0.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
    }

    js(IR) {
        browser()
        nodejs()
    }
}
