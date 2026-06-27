import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidLibrary {
        namespace = "org.ahugenb.movieswiper.data.api"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonMain.dependencies {
            api(project(":core:models"))
            api(project(":core:common"))
            api(libs.ktorClientCore)
            api(libs.ktorClientContentNegotiation)
            api(libs.ktorSerializationKotlinxJson)
            implementation(libs.ktorClientLogging)
            implementation(libs.koinCore)
            implementation(libs.napier)
        }
        androidMain.dependencies {
            implementation(libs.ktorClientOkhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktorClientOkhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktorClientDarwin)
        }
    }
}

buildkonfig {
    packageName = "org.ahugenb.movieswiper.data.api"
    objectName = "BuildConfig"
    
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "TMDB_API_KEY", "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyYWRjMWE3MzE0M2VmMWRhNWU3ZTcwNThjMTBjZjhiMiIsIm5iZiI6MTY1OTU2MzExMy43MjEsInN1YiI6IjYyZWFlYzY5MWJmMjY2MDA2MDJlZmI4NyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.nCz5SJE3omg3UCXXvf7z1PsAe0v8O7mJld8w_Hx0Wxc")
        buildConfigField(FieldSpec.Type.STRING, "BACKEND_BASE_URL", "http://10.0.2.2:8081")
    }
}