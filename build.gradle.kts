plugins {
    kotlin("multiplatform") version "1.5.0"
    id("com.android.library")
    id("maven-publish")
}

val kotlin_version = "1.5.0"
val ktor_version = "1.5.2"
val thelema_group = "app.thelema"
val thelema_version = "0.6.0"

group = "app.thelema"
version = "0.6.0"

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()

    js {
        browser {}
    }

    android {
        publishLibraryVariants("release", "debug")
    }
    
    sourceSets {
        val commonMain by getting

        val jvmCommonMain by creating {
            dependsOn(commonMain)

            dependencies {
                implementation("com.github.cliftonlabs:json-simple:3.1.1")
                implementation("org.ode4j:core:0.4.0")

                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("io.ktor:ktor-client-websockets:$ktor_version")
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)

            dependencies {
                implementation(files("libs/ogg-jvm-lib-0.1.0.jar"))

                val lwjglPrefix = "org.lwjgl:lwjgl"
                val lwjglVersion = "3.2.3"
                implementation("$lwjglPrefix-bom:$lwjglVersion")
                implementation("$lwjglPrefix:$lwjglVersion")
                implementation("$lwjglPrefix-glfw:$lwjglVersion")
                implementation("$lwjglPrefix-jemalloc:$lwjglVersion")
                implementation("$lwjglPrefix-openal:$lwjglVersion")
                implementation("$lwjglPrefix-opengl:$lwjglVersion")
                implementation("$lwjglPrefix-stb:$lwjglVersion")

                val platforms = arrayOf("natives-linux", "natives-windows", "natives-windows-x86", "natives-macos")
                platforms.forEach {
                    implementation("$lwjglPrefix:$lwjglVersion:$it")
                    implementation("$lwjglPrefix-glfw:$lwjglVersion:$it")
                    implementation("$lwjglPrefix-jemalloc:$lwjglVersion:$it")
                    implementation("$lwjglPrefix-openal:$lwjglVersion:$it")
                    implementation("$lwjglPrefix-opengl:$lwjglVersion:$it")
                    implementation("$lwjglPrefix-stb:$lwjglVersion:$it")
                }
            }

//            val jvmJar by tasks.getting(Jar::class) {
//                doFirst {
//                    from(configurations.getByName("jvmRuntimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
//                }
//            }
        }

        val jsMain by getting

        val androidMain by getting {
            dependsOn(jvmCommonMain)
        }
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
}