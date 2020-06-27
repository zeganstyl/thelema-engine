plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
}

val thelemaGroup: String by project
group = thelemaGroup

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":thelema-jvm"))
    implementation(project(":thelema-core"))

    testImplementation(project(path = ":thelema-core-tests"))

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    val lwjglVersion = "3.2.3"
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-jemalloc")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")

    val platforms = arrayOf("natives-linux", "natives-windows", "natives-windows-x86", "natives-macos")
    platforms.forEach {
        implementation("org.lwjgl", "lwjgl", classifier = it)
        implementation("org.lwjgl", "lwjgl-glfw", classifier = it)
        implementation("org.lwjgl", "lwjgl-jemalloc", classifier = it)
        implementation("org.lwjgl", "lwjgl-openal", classifier = it)
        implementation("org.lwjgl", "lwjgl-opengl", classifier = it)
        implementation("org.lwjgl", "lwjgl-stb", classifier = it)
    }
}

tasks {
    jar {
        from ({
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory) {
                    it
                } else {
                    val name = it.name.toLowerCase()
                    if (name.contains("lwjgl") ||
                        name.contains("json") ||
                        name.contains("thelema")) {
                        zipTree(it)
                    } else {
                        null
                    }
                }
            }
        })
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
