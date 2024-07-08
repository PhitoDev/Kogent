import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.ralphdugue.kogent"
version = "0.1-PRE-ALPHA"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                api(project.dependencies.platform(libs.koin.bom))
                api(project.dependencies.platform(libs.koin.annotations.bom))
                api("io.insert-koin:koin-core")
                api("io.insert-koin:koin-core-coroutines")
                api("io.insert-koin:koin-annotations")
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.bundles.ktor)
                implementation(libs.kache)
            }
        }
        val commonTest by getting {
            dependencies {
                api(project.dependencies.platform(libs.test.junit.bom))
                api("org.junit.jupiter:junit-jupiter")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
                api(libs.bundles.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.zaxxer:HikariCP:5.1.0")
                implementation("com.h2database:h2:2.2.224")
                implementation("org.postgresql:postgresql:42.7.3")
                implementation("io.milvus:milvus-sdk-java:2.4.0")
                implementation("org.json:json:20231013")
                implementation("org.slf4j:slf4j-api:2.0.12")
            }
        }
        val jvmTest by getting
        val androidMain by getting
        val androidUnitTest by getting
    }
}

android {
    namespace = "com.ralphdugue.kogent.core"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    // task("testClasses")
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

// java {
//    withSourcesJar()
// }

apply(from = file("../gradle/publish.gradle.kts"))
