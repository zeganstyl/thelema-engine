plugins {
    kotlin("multiplatform")
    id("com.android.application")
}

group = "app.thelema"

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    google()
}

val thelema_prefix = "app.thelema:thelema-engine"
val thelema_version = "0.6.0"
val ktor_version = "1.5.2"

kotlin {
    jvm()

    jvm("jvmTestServer")

    android()

    js {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val assets by creating

        val commonMain by getting {
            dependencies {
                implementation(project(rootProject.path))
            }
        }

        val androidMain by getting {
            dependsOn(assets)

            dependencies {
                implementation(project(rootProject.path))
            }
        }

        val jvmMain by getting {
            dependencies {
                rootProject.allprojects.forEach { println(it.path) }
                implementation(project(rootProject.path))
            }

            val jvmJar by tasks.getting(Jar::class) {
                doFirst {
                    manifest {
                        attributes(
                            "Main-Class" to "Main"
                        )
                    }

                    from(configurations.getByName("jvmRuntimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
                }
            }
        }

        val jvmTestServerMain by getting {
            dependsOn(jvmMain)

            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-websockets:$ktor_version")

                implementation("org.slf4j:slf4j-simple:1.7.30")
            }
        }

        val jsMain by getting {
            dependsOn(assets)

            dependencies {
                implementation(project(rootProject.path))
            }
        }
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        applicationId = "app.thelema.test.AndroidMain"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}