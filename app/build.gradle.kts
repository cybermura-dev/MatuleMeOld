import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "ru.takeshiko.matuleme"
    compileSdk = 35

    val localProperties = Properties().apply {
        file(rootProject.file("local.properties")).inputStream().use { load(it) }
    }
    val supabaseUrl: String = localProperties.getProperty("supabaseUrl", "")
    val supabaseKey: String = localProperties.getProperty("supabaseKey", "")

    defaultConfig {
        applicationId = "ru.takeshiko.matuleme"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.1"


        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.security.crypto)

    implementation(libs.shimmer)
    implementation(libs.jsr305)
    implementation(libs.glide)

    implementation(libs.android.material)
    implementation(libs.android.ktor.client)
    implementation(libs.android.yookassa.sdk)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.realtime.kt)
    implementation(libs.supabase.storage.kt)

}