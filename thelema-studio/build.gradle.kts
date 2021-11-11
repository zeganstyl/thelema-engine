/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("multiplatform")
}

group = thelema_group

kotlin {
    jvm()

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(rootProject.path))
                implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlin_version")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(rootProject.path))

                implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlin_version")
                implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlin_version")

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

            val jvmMainClass = "app.thelema.studio.ThelemaStudioJvm"

            val jvmJar by tasks.getting(Jar::class) {
                doFirst {
                    manifest {
                        attributes(
                            "Main-Class" to jvmMainClass
                        )
                    }

                    from(configurations.getByName("jvmRuntimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
                }
            }

            val run by tasks.creating(JavaExec::class) {
                dependsOn(tasks.getByName("jvmMainClasses"))
                main = jvmMainClass
                classpath = configurations.getByName("jvmRuntimeClasspath") +
                        files("$buildDir/classes/kotlin/jvm/main") +
                        commonMain.resources.sourceDirectories
            }
        }
    }
}
