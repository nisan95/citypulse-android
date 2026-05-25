import java.util.Properties

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.citypulse.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.citypulse.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Clés sécurisées depuis local.properties
        val mapsKey = localProps.getProperty("MAPS_API_KEY", "")
        val baseUrl = localProps.getProperty("BASE_URL", "")
        val apiKey = localProps.getProperty("API_KEY", "")

        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    signingConfigs {
        create("release") {
            // ✅ Réutiliser localProps déjà chargé en haut du bloc Android
            val keystorePath = localProps.getProperty("KEYSTORE_PATH", "")
            val keystorePass = localProps.getProperty("KEYSTORE_PASSWORD", "")
            val alias = localProps.getProperty("KEY_ALIAS", "")
            val keyPass = localProps.getProperty("KEY_PASSWORD", "")

            storeFile = if (keystorePath.isNotEmpty()) file(keystorePath) else null
            storePassword = keystorePass
            keyAlias = alias
            keyPassword = keyPass
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    // ── Android Core ─────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ── Lifecycle ───────────────────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    // ── Coroutines ──────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ── Navigation ──────────────────────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ── Room ────────────────────────────────────────────────────
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    kapt("androidx.room:room-compiler:$roomVersion")

    // ── Retrofit + OkHttp ───────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Google Location ─────────────────────────────────────────
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ── Google Maps ─────────────────────────────────────────────
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // ── Images ──────────────────────────────────────────────────
    implementation("io.coil-kt:coil:2.6.0")

    // ── DataStore ───────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Tests ───────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test:core:1.6.1")

    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}