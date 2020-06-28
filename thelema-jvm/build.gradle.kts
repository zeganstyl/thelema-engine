plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
}

val thelemaGroup: String by project
group = thelemaGroup

val gitRepositoryUrl: String by project

val verName = "0.1.0"
version = verName

dependencies {
    api(project(":thelema-core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // https://mvnrepository.com/artifact/com.esotericsoftware/jsonbeans
    api("com.esotericsoftware", "jsonbeans", "0.7")

    // https://mvnrepository.com/artifact/com.github.cliftonlabs/json-simple
    api("com.github.cliftonlabs", "json-simple", "3.1.1")
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
        name = "thelema-jvm"
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
