plugins {
    alias(libs.plugins.multiplatform)
}

group = "dev.intsuc"
version = "1.3.10"

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

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinxCoroutinesCore)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinxCoroutinesTest)
                implementation(kotlin("test"))
            }
        }
    }
}
