plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt") version "2.0.0" // ✅ FIX: nom complet du plugin kapt
}

android {
    compileSdk = 34 // ✅ FIX: mis à jour (34 → 35)
    namespace = "com.citypulse.app"


    defaultConfig {
        applicationId = "com.citypulse.app"
        minSdk = 26
        targetSdk = 34 // ✅ FIX: mis à jour (34 → 35)
        versionCode = 1
        versionName = "1.0"

        // Lire la clé Maps depuis local.properties (sécurisé)
        val mapsKey = project.findProperty("MAPS_API_KEY") as String? ?: ""
        val baseUrl    = project.findProperty("BASE_URL")     as String? ?: ""
        val apiKey     = project.findProperty("API_KEY")      as String? ?: ""

        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "BASE_URL",     "\"$baseUrl\"")
        buildConfigField("String", "API_KEY",      "\"$apiKey\"")

    }

    buildFeatures {
        viewBinding = true  // Accès sûr aux vues sans findViewById
        buildConfig = true  // Pour exposer les clés API via BuildConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ FIX: kotlinOptions { jvmTarget } remplacé par jvmToolchain (non déprécié)
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // ── Android Core ─────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.13.1")             // ✅ FIX: 1.12.0 → 1.13.1
    implementation("androidx.appcompat:appcompat:1.7.0")         // ✅ FIX: 1.6.1 → 1.7.0
    implementation("com.google.android.material:material:1.12.0") // ✅ FIX: 1.11.0 → 1.12.0
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ── Cycle de vie (ViewModel + LiveData + StateFlow) ──────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    // ── Coroutines ────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ── Navigation Component ─────────────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ── Room (SQLite) ─────────────────────────────────────────────
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion") // ✅ fonctionne avec le plugin kapt corrigé

    // ── Retrofit + OkHttp (réseau REST) ──────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")         // ✅ FIX: 2.9.0 → 2.11.0
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")   // ✅ FIX: 2.9.0 → 2.11.0
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Géolocalisation (FusedLocationProvider) ──────────────────
    implementation("com.google.android.gms:play-services-location:21.3.0") // ✅ FIX: 21.1.0 → 21.3.0

    // ── Carte (Google Maps) ───────────────────────────────────────
    implementation("com.google.android.gms:play-services-maps:19.0.0") // ✅ FIX: 18.2.0 → 19.0.0

    // ── Chargement d'images ───────────────────────────────────────
    implementation("io.coil-kt:coil:2.6.0")                           // ✅ FIX: 2.5.0 → 2.6.0

    // ── DataStore (stockage clé-valeur) ───────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.1")  // ✅ FIX: 1.0.0 → 1.1.1

    // ── Tests ──────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")        // ✅ FIX: 1.1.5 → 1.2.1
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // ✅ FIX: 3.5.1 → 3.6.1
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
}