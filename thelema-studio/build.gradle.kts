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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(rootProject.path))
                implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlin_version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(rootProject.path))

                implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlin_version")
                implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlin_version")

                lwjglImplementations.forEach { implementation(it) }
                implementation("$lwjgl_prefix-assimp:$lwjgl_version")
                lwjglNatives.forEach { implementation("$lwjgl_prefix-assimp:$lwjgl_version:$it") }

                // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
                implementation("ch.qos.logback:logback-classic:1.2.10")
            }

            val jvmMainClass = "app.thelema.studio.ThelemaStudioJvm"

            val jvmJar by tasks.getting(Jar::class) {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE

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
                jvmArgs = listOf("-XstartOnFirstThread")
            }
        }
    }
}
