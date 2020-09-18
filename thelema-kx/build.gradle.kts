plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    val nativeDependenciesLink = { target: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests ->
        val main by target.compilations.getting
        main.cinterops {
            val glfw by creating {
                val includeDir = when (target.preset) {
                    presets["linuxX64"] -> "/usr/include"
                    else -> throw NotImplementedError()
                }
                includeDirs.headerFilterOnly(includeDir, "$projectDir/src/nativeInterop/cinterop")
            }
            val al by creating {
                val includeDir = when (target.preset) {
                    presets["linuxX64"] -> "/usr/include"
                    else -> throw NotImplementedError()
                }
                includeDirs.headerFilterOnly(includeDir)
            }
        }
    }

    js {
        browser {
        }
    }
    js("jsTests") {
        browser {
        }
    }
    wasm32("wasm") {
        val main by compilations.getting
        main.cinterops {}

        binaries {
            executable {
                entryPoint = "org.ksdfv.thelema.kxwasm.main"
            }
        }
    }

    linuxX64("native") {
        nativeDependenciesLink(this)
    }

    linuxX64("nativeTests") {
        nativeDependenciesLink(this)
        binaries {
            executable("thelema-kx") {
                entryPoint = "org.ksdfv.thelema.kxnative.test.main"
            }
            executable("single-test") {
                entryPoint = "org.ksdfv.thelema.kxnative.test.mainSingle"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTests by creating {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        js("jsTests").compilations["main"].defaultSourceSet {
            dependsOn(commonTests)
            dependsOn(jsMain)
        }
        linuxX64("nativeTests").compilations["main"].defaultSourceSet {
            dependsOn(commonTests)
            dependsOn(linuxX64("native").compilations["main"].defaultSourceSet)
        }
    }
}
