plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidLibrary {
        namespace = "org.ahugenb.movieswiper.data.localdb"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:models"))
            implementation(project(":data:api"))
            implementation(libs.sqldelightRuntime)
            implementation(libs.sqldelightCoroutinesExtensions)
            implementation(libs.koinCore)
            implementation(libs.kotlinxDatetime)
        }
        androidMain.dependencies {
            implementation(libs.sqldelightAndroidDriver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelightNativeDriver)
        }
    }
}

sqldelight {
    databases {
        create("MovieDatabase") {
            packageName.set("org.ahugenb.movieswiper.data.localdb")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.3.2")
        }
    }
}