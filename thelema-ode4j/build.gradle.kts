plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
}

val thelemaGroup: String by project
group = thelemaGroup

val gitRepositoryUrl: String by project

val thelemaVersion: String by project
val verName = thelemaVersion
version = verName

repositories {
    maven {
        url = uri("https://www.beatunes.com/repo/maven2/")
    }
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":thelema-core"))

    implementation(files("libs/core-0.4.1-SNAPSHOT.jar"))
    //implementation(group = "org.ode4j", name = "core", version = "0.4.1+")

    testImplementation(project(":thelema-lwjgl3"))
    testImplementation(project(":thelema-core-tests"))
}

tasks {
    jar {
        from ({
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory) {
                    it
                } else {
                    val name = it.name.toLowerCase()
                    if (name.contains("core-0.4")) {
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

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.getByName("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

bintray {
    // user and key properties must be saved in user home (by default ~/.gradle/gradle.properties)
    user = project.property("BINTRAY_USER") as String
    key = project.property("BINTRAY_KEY") as String
    override = true
    publish = true
    setPublications("mavenJava")
    pkg.apply {
        repo = "thelema-engine"
        name = "thelema-ode4j"
        setLicenses("Apache-2.0")
        vcsUrl = gitRepositoryUrl

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
