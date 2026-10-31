import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "sessionable.js"
            }
        }
    }

    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "sessionable.js"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":app:shared"))
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "org.jetbrains.kotlinconf.web.generated.resources"
}
