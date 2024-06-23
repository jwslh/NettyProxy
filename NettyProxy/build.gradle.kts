import com.google.wireless.android.sdk.stats.GradleBuildVariant.KotlinOptions
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_11.ordinal + 1)
}

dependencies {
    api(libs.netty.all)
    api(project(":common"))
}