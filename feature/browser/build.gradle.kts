plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "org.ahugenb.movieswiper.feature.browser"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonMain.dependencies {
            implementation(project(":ui"))
            implementation(project(":core:logic"))
            implementation(project(":core:common"))
            implementation(project(":core:models"))
            implementation(project(":data:api"))
            implementation(project(":data:localdb"))
            implementation(libs.decompose)
            implementation(libs.decomposeExtensionsCompose)
            implementation(libs.koinCore)
        }
    }
}