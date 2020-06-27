plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
}

val thelemaGroup: String by project
group = thelemaGroup

version = "0.0.1"

dependencies {
    api(project(":thelema-core"))
    api("org.jetbrains.kotlin:kotlin-stdlib")

    // https://mvnrepository.com/artifact/com.esotericsoftware/jsonbeans
    api("com.esotericsoftware", "jsonbeans", "0.7")

    // https://mvnrepository.com/artifact/com.github.cliftonlabs/json-simple
    api("com.github.cliftonlabs", "json-simple", "3.1.1")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
