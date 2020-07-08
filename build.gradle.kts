buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")
    }
}

val thelemaGroup by extra("org.ksdfv.thelema")
val thelemaVersion by extra("0.2.0")
val gitRepositoryUrl by extra("https://github.com/zeganstyl/thelema-engine")

allprojects {
    repositories {
        jcenter()
        mavenLocal()
    }
}
