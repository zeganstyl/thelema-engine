plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
    //id("org.jetbrains.dokka") version "0.10.1"
}

val thelemaGroup: String by project
group = thelemaGroup

version = "0.0.1"

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")

    // https://mvnrepository.com/artifact/com.github.cliftonlabs/json-simple
    api("com.github.cliftonlabs", "json-simple", "3.1.1")
}

val sourcesJar = tasks.create("sourcesJar", Jar::class.java) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

//val dokkaJavadoc = tasks.create("dokkaJavadoc", org.jetbrains.dokka.gradle.DokkaTask::class.java) {
//    outputFormat = "javadoc"
//    outputDirectory = "$buildDir/javadoc"
//}
//
//val javadocJar = tasks.create("javadocJar", Jar::class.java) {
//    dependsOn(dokkaJavadoc)
//    archiveClassifier.set("javadoc")
//    from("$buildDir/javadoc")
//}

//artifacts {
//    archives sourcesJar
//            archives javadocJar
//}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
            //artifact(javadocJar)

            pom {
                name.set("thelema-engine")
                url.set("")
            }
        }
    }
}
