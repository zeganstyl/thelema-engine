plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
}

val thelemaGroup: String by project
group = thelemaGroup

val gitRepositoryUrl: String by project

val verName = "0.2.0"
version = verName

repositories {
    mavenCentral()
}

dependencies {
    api(project(":thelema-jvm"))
    api(project(":thelema-core"))

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

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJavadoc by tasks.registering(org.jetbrains.dokka.gradle.DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

bintray {
    // user and key properties must be saved in user home (by default ~/.gradle/gradle.properties)
    user = project.property("BINTRAY_USER") as String
    key = project.property("BINTRAY_KEY") as String
    setPublications("mavenJava")
    pkg.apply {
        repo = "thelema-engine"
        name = "thelema-lwjgl3"
        setLicenses("Apache-2.0")
        vcsUrl = gitRepositoryUrl
        githubRepo = gitRepositoryUrl

        version.apply {
            name = verName
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set("thelema-engine")
                url.set(gitRepositoryUrl)

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("zeganstyl")
                        name.set("Anton Trushkov")
                        email.set("zeganstyl@gmail.com")
                    }
                }

                scm {
                    url.set(gitRepositoryUrl)
                }
            }
        }
    }
}
