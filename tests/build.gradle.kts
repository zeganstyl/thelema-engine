plugins {
    kotlin("multiplatform")
    id("com.android.application")
}

group = thelema_group
version = thelema_version

val lwjgl3_package = "$thelema_group.lwjgl3"

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

kotlin {
    jvm()

    jvm("jvmTestServer")

    linuxX64 {
        binaries {
            executable()
        }
    }

    android()

    js {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(rootProject.path))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project(rootProject.path))
            }
        }

        val linuxX64Main by getting {
            dependencies {
                implementation(project(rootProject.path))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(rootProject.path))
                implementation("org.slf4j:slf4j-simple:1.7.30")
                implementation("org.recast4j:recast:1.5.0")
                implementation("org.recast4j:detour:1.5.0")
                implementation("org.recast4j:detour-crowd:1.5.0")
                implementation("org.recast4j:detour-tile-cache:1.5.0")
                implementation("org.recast4j:detour-extras:1.5.0")
                implementation("org.recast4j:detour-dynamic:1.5.0")

                val platforms = arrayOf("natives-linux", "natives-windows", "natives-windows-x86", "natives-macos")
                platforms.forEach {
                    implementation("$lwjgl_prefix:$lwjgl_version:$it")
                    implementation("$lwjgl_prefix-glfw:$lwjgl_version:$it")
                    implementation("$lwjgl_prefix-jemalloc:$lwjgl_version:$it")
                    implementation("$lwjgl_prefix-openal:$lwjgl_version:$it")
                    implementation("$lwjgl_prefix-opengl:$lwjgl_version:$it")
                    implementation("$lwjgl_prefix-stb:$lwjgl_version:$it")
                }
            }

            val jvmJar by tasks.getting(Jar::class) {
                doFirst {
                    manifest {
                        attributes(
                            "Main-Class" to "$lwjgl3_package.MainTestJvm"
                        )
                    }

                    from(configurations.getByName("jvmRuntimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
                }
            }

            val run by tasks.creating(JavaExec::class) {
                dependsOn(tasks.getByName("jvmMainClasses"))
                main = "$lwjgl3_package.MainTestJvm"
                classpath = configurations.getByName("jvmRuntimeClasspath") +
                        files("$buildDir/classes/kotlin/jvm/main") +
                        commonMain.resources.sourceDirectories
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
            dependencies {
                implementation(project(rootProject.path))
            }
        }
    }
}

android {
    compileSdkVersion(31)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        applicationId = "app.thelema.test.android.AndroidMain"
        minSdkVersion(21)
        targetSdkVersion(31)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
