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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    api(project(":thelema-core"))
    testImplementation(project(":thelema-core-tests"))

    // https://mvnrepository.com/artifact/com.github.cliftonlabs/json-simple
    api("com.github.cliftonlabs:json-simple:3.1.1")

    api("org.teavm:teavm-classlib:0.6.1")
    api("org.teavm:teavm-jso-apis:0.6.1")
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
    override = true
    publish = true
    setPublications("mavenJava")
    pkg.apply {
        repo = "thelema-engine"
        name = "thelema-teavm"
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
                description.set("Backend-module that implements core interfaces with TeaVM")

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

fun checkTeavmDependency(file: File): Boolean {
    val name = file.name
    return !(name.contains("teavm") ||
            name.contains("gson") ||
            name.contains("jzlib") ||
            name.contains("joda-time") ||
            name.contains("annotations")
            )
}

val copyTestRuntimeDependencies = tasks.register<Copy>("unpackRuntimeDependencies") {
    dependsOn(tasks.build)

    configurations.testRuntimeClasspath.get().all {
        if (checkTeavmDependency(it)) {
            from(zipTree(it).matching {
                include("**/*.class")
            })
        }
        true
    }

    includeEmptyDirs = false

    into("$buildDir/classes/dependencies")
}

val copyTestRuntimeResources = tasks.register<Copy>("copyTestRuntimeResources") {
    configurations.testRuntimeClasspath.get().all {
        if (checkTeavmDependency(it)) {
            from(zipTree(it).matching {
                exclude("**/*.class")
                exclude("kotlin/**")
                exclude("META-INF/**")
            })
        }
        true
    }

    includeEmptyDirs = false

    into("$buildDir/teavm")
}

tasks.register("teavm-compile-tests", Exec::class) {
    dependsOn(copyTestRuntimeDependencies, copyTestRuntimeResources)

    val jars = ArrayList<File>()

    configurations.testRuntimeClasspath.get().all {
        if (checkTeavmDependency(it)) jars.add(it)
        true
    }

    println("=== dependencies ===")
    jars.forEach { println(it.name) }

    /*
    usage: java org.teavm.cli.TeaVMRunner [OPTIONS] [qualified.main.Class]
 -c,--cachedir <directory>          Incremental build cache directory
 -d,--targetdir <directory>         a directory where to put generated
                                    files (current directory by default)
 -e,--entry-point <name>            Entry point name in target language
                                    (main by default)
 -f,--targetfile <file>             a file where to put decompiled classes
                                    (classes.js by default)
 -g,--debug                         Generate debug information
 -G,--sourcemaps                    Generate source maps
 -i,--incremental                   Incremental build
 -m,--minify                        causes TeaVM to generate minimized
                                    JavaScript file
    --max-toplevel-names <number>   Maximum number of names kept in
                                    top-level scope (other will be put in
                                    a separate object. 10000 by default.
    --min-heap <size>               Minimum heap size in megabytes (for C
                                    and WebAssembly)
    --no-longjmp                    Don't use setjmp/longjmp functions to
                                    emulate exceptions (C target)
 -O <number>                        optimization level (1-3)
 -p,--classpath <classpath>         Additional classpath that will be
                                    reloaded by TeaVM each time in wait
                                    mode
    --preserve-class <class name>   Tell optimizer to not remove class, so
                                    that it can be found by Class.forName
 -t <target>                        target type (javascript/js,
                                    webassembly/wasm, C)
 -w,--wait                          Wait for command after compilation, in
                                    order to enable hot recompilation
    --wasm-version <version>        WebAssembly binary version (currently,
                                    only 1 is supported)
     */

    commandLine = ArrayList<String>().apply {
        add("java")
        add("-jar")
        add("teavm.jar")
        add("--classpath")
        add("$buildDir/classes/dependencies")
        add("$buildDir/classes/kotlin/main")
        add("$buildDir/classes/kotlin/test")

        jars.forEach {
            add(it.path)
        }

        add("--targetdir")
        add("build/teavm")
        add("--debug")
        add("--sourcemaps")
        add("org.ksdfv.thelema.teavm.test.MainTeaVMTests")
    }
}
