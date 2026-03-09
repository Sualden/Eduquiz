@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    kotlin("plugin.serialization") version "2.0.21"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.android.driver)
            implementation(libs.androidx.room.runtime.android)
            implementation(libs.barcode.scanning.v1720)

            implementation( libs.androidx.camera.camera2)
            implementation (libs.androidx.camera.lifecycle)
            implementation( libs.androidx.camera.view)

            // ML Kit barcode
            implementation (libs.mlkit.barcode.scanning)

            // Jetpack Compose
            //implementation(platform("androidx.compose:compose-bom:2025.12.00"))
            implementation(libs.androidx.ui)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.material.icons.extended) // Use a version matching your Compose BOM\

            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.androidx.activity.compose.v182)

            implementation("io.ktor:ktor-client-core:3.0.0")
            implementation("io.ktor:ktor-client-cio:3.0.0") // Required for 'CIO'
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // JetBrains Lifecycle (KMP versions)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.2")


            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // 🟦 SQLDelight Runtime (COMMON)
            implementation("app.cash.sqldelight:runtime:2.0.2")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

            // 🕒 DateTime (Fixes 'Unresolved reference System')
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            implementation("at.favre.lib:bcrypt:0.10.2")
            // Serialization

            // Password Hashing
            implementation("org.mindrot:jbcrypt:0.4")
            implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")
            // -----------------------------
            // KOTLIN & COROUTINES
            // -----------------------------
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")


        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            // KTOR SERVER (v3.3.3)
            // -----------------------------
            // Consolidated all modules to 3.3.3 to avoid crash
            implementation("io.ktor:ktor-server-core:3.3.3")
            implementation("io.ktor:ktor-server-netty:3.3.3")
            implementation("io.ktor:ktor-server-content-negotiation:3.3.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")

            // Updated these from 2.3.12 to 3.3.3 to match core
            implementation("io.ktor:ktor-server-cors:3.3.3")
            implementation("io.ktor:ktor-server-status-pages:3.3.3")
            implementation("io.ktor:ktor-server-call-logging:3.3.3")
            implementation("io.ktor:ktor-server-auth:3.3.3")
            implementation("io.ktor:ktor-server-auth-jwt:3.3.3")

            // -----------------------------
            // KTOR CLIENT (v3.3.3)
            // -----------------------------
            // Updated Client to match Server version
            implementation("io.ktor:ktor-client-core:3.3.3")
            implementation("io.ktor:ktor-client-cio:3.3.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.3")

            // -----------------------------
            // DATABASE (SQLDelight)
            // -----------------------------
            implementation("app.cash.sqldelight:sqlite-driver:2.0.2")

            // -----------------------------
            // UTILITIES & SECURITY
            // -----------------------------
            implementation(libs.jbcrypt) // Encryption
            implementation(libs.java.jwt) // Auth0 JWT
            implementation("ch.qos.logback:logback-classic:1.5.21") // Logging

            // -----------------------------
            // UI
            // -----------------------------
            implementation(libs.material.icons.extended.desktop)
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            // CORRECT
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
            implementation("org.jetbrains.compose.foundation:foundation:1.9.3")
            implementation("com.google.zxing:core:3.5.4")
        }
    }
}


android {
    namespace = "com.dens.eduquiz"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.dens.eduquiz"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.dens.eduquiz.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.dens.eduquiz"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.dens.eduquiz.`database`")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schemas"))
        }
    }
}