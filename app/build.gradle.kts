import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.redstar.redefinencm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.redstar.redefinencm"
        minSdk = 29
        targetSdk = 35
        versionCode = generateVersionCode()
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionNameSuffix = "EarlyAccess_${getGitSha()}"
        buildConfigField("String", "GIT_SHA", "\"${getGitSha()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.common)
    implementation(libs.exoplayer.core)
    implementation(libs.extension.mediasession)
    implementation(libs.extension.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.converter.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.androidx.palette)
    implementation(libs.lyric.getter.api)
}

fun getGitSha(): String {
    return try {
        val stdout = "git rev-parse HEAD".executeCommand()
        stdout.trim()
    } catch (e: Exception) {
        "GIT_FAILED"
    }
}

fun String.executeCommand(): String {
    return ProcessBuilder(*this.split(" ").toTypedArray())
        .directory(File(project.rootDir.absolutePath))
        .start()
        .inputStream
        .bufferedReader()
        .readText()
}

fun generateVersionCode(): Int {
    return SimpleDateFormat("yyMMddHH", Locale.ROOT).format(Date()).toInt()
}
