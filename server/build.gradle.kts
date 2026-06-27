plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.kotlinxSerialization)
}

group = "org.ahugenb.movieswiper"
version = "1.0.0"

application {
    mainClass.set("org.ahugenb.movieswiper.server.ApplicationKt")
}

dependencies {
    implementation(project(":core:models"))
    implementation(project(":core:logic"))
    implementation(project(":data:api"))
    
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerContentNegotiation)
    implementation(libs.ktorServerStatusPages)
    implementation(libs.ktorSerializationKotlinxJson)
    implementation(libs.ktorServerCors)
    implementation(libs.ktorServerCallLogging)
    implementation(libs.logback)
    implementation(libs.koinCore)
    implementation(libs.napier)
}

kotlin {
    jvmToolchain(21)
}