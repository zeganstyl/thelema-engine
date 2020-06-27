buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
}

val thelemaGroup by extra("org.ksdfv.thelema")
val thelemaVersion by extra("0.0.1")

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }
}
