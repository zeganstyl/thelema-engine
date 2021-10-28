plugins {
    kotlin("multiplatform") version kotlin_version
    id("com.android.library")
    id("maven-publish")
}

group = thelema_group
version = thelema_version

kotlin {
    jvm()

    linuxX64 {
        val target = this
        val main by target.compilations.getting
        main.cinterops {
            val glfw by creating {
                val includeDir = when (target.preset) {
                    presets["linuxX64"] -> "/usr/include"
                    else -> throw NotImplementedError()
                }
                includeDirs.headerFilterOnly(includeDir, "$projectDir/src/nativeInterop/cinterop")
            }
//            val al by creating {
//                val includeDir = when (target.preset) {
//                    presets["linuxX64"] -> "/usr/include"
//                    else -> throw NotImplementedError()
//                }
//                includeDirs.headerFilterOnly(includeDir)
//            }
        }
    }

    js {
        browser {}
    }

    android {
        publishLibraryVariants("release", "debug")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    
    sourceSets {
        val commonMain by getting

        val jvmCommonMain by creating {
            dependsOn(commonMain)

            dependencies {
                implementation("com.github.cliftonlabs:json-simple:3.1.1")
                api("org.ode4j:core:0.4.0")
                api("org.recast4j:recast:1.5.0")

                api("io.ktor:ktor-client-core:$ktor_version")
                api("io.ktor:ktor-client-cio:$ktor_version")
                api("io.ktor:ktor-client-websockets:$ktor_version")
            }
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)

            dependencies {
                implementation(files("libs/ogg-jvm-lib-0.1.0.jar"))

                val lwjglPrefix = "org.lwjgl:lwjgl"
                val lwjglVersion = "3.2.3"
                api("$lwjglPrefix-bom:$lwjglVersion")
                api("$lwjglPrefix:$lwjglVersion")
                api("$lwjglPrefix-glfw:$lwjglVersion")
                api("$lwjglPrefix-jemalloc:$lwjglVersion")
                api("$lwjglPrefix-openal:$lwjglVersion")
                api("$lwjglPrefix-opengl:$lwjglVersion")
                api("$lwjglPrefix-stb:$lwjglVersion")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    repositories {
        maven {
            setUrl("https://maven.pkg.github.com/zeganstyl/thelema-engine")
            name = "github"
            credentials(PasswordCredentials::class)
        }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
