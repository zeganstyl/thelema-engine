package app.thelema.studio

import app.thelema.THELEMA

object CreateProjectTool {
    const val kotlinVersionDefault = "1.6.10"
    const val lwjglVersionDefault = "3.3.0"

    val lwjglPlatformsAll = arrayOf(
        LwjglNativeDependency("Linux x64", "linux", true),
        LwjglNativeDependency("Linux arm64", "linux-arm64"),
        LwjglNativeDependency("Linux arm32", "linux-arm32"),
        LwjglNativeDependency("macOS x64", "macos", true),
        LwjglNativeDependency("macOS arm64", "macos-arm64", true),
        LwjglNativeDependency("Windows x64", "windows", true),
        LwjglNativeDependency("Windows x86", "windows-x86"),
        LwjglNativeDependency("Windows arm64", "windows-arm64")
    )

    const val defaultSceneScript = """import app.thelema.ecs.Component
import app.thelema.ecs.IEntity
import app.thelema.ecs.UpdatableComponent
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.transformNode

class NewScene : Component(), UpdatableComponent {
    override val componentName: String
        get() = "NewScene"

    var boxNode: ITransformNode? = null

    override fun onAttachedToEntity(entity: IEntity, old: IEntity?) {
        println("Hello Script!")

        boxNode = entity.entity("Box").transformNode()
    }

    override fun updateComponent(delta: Float) {
        // TODO move your box
        // boxNode?.translateForward(delta)
    }
}
"""

    const val defaultInitScripts = """import app.thelema.ecs.ECS

fun initScripts() = ECS.apply {
    descriptor({ NewScene() }) {
        ref(NewScene::boxNode)
    }
}
"""

    const val emptyInitScripts = """import app.thelema.ecs.ECS

fun initScripts() = ECS.apply {
    // TODO script components
}
"""

    fun jvmBuildGradle(
        mainClassPath: String,
        kotlinVersion: String,
        lwjglVersion: String,
        lwjglPlatforms: List<LwjglNativeDependency>
    ): String = """plugins {
    kotlin("jvm") version "$kotlinVersion"
}

repositories {
    mavenCentral()
    mavenLocal()
}

${jvmDependencies(lwjglVersion, lwjglPlatforms)}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "$mainClassPath"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
"""

    fun jvmDependencies(lwjglVersion: String, lwjglPlatforms: List<LwjglNativeDependency>): String = """dependencies {
    testImplementation(kotlin("test"))
    implementation("app.thelema:thelema:${THELEMA.verStr}")

    val lwjgl_version = "$lwjglVersion"

    val platforms = arrayOf(${lwjglPlatformsToString(lwjglPlatforms)})
    platforms.forEach {
        implementation("org.lwjgl:lwjgl:${'$'}lwjgl_version:${'$'}it")
        implementation("org.lwjgl:lwjgl-glfw:${'$'}lwjgl_version:${'$'}it")
        implementation("org.lwjgl:lwjgl-jemalloc:${'$'}lwjgl_version:${'$'}it")
        implementation("org.lwjgl:lwjgl-openal:${'$'}lwjgl_version:${'$'}it")
        implementation("org.lwjgl:lwjgl-opengl:${'$'}lwjgl_version:${'$'}it")
        implementation("org.lwjgl:lwjgl-stb:${'$'}lwjgl_version:${'$'}it")
    }
}"""

    private fun lwjglPlatformsToString(lwjglPlatforms: List<LwjglNativeDependency>): String {
        var platforms = ""
        for (i in lwjglPlatforms.indices) {
            if (i > 0) platforms += ", "
            platforms += "\"natives-${lwjglPlatforms[i].libName}\""
        }
        return platforms
    }

    fun mainAppLauncherDefault(title: String, scripts: Boolean): String = """import app.thelema.app.APP
import app.thelema.lwjgl3.JvmApp
import app.thelema.res.runMainScene

fun main() {
    val app = JvmApp {
        width = 1280
        height = 720
        msaaSamples = 4
        title = "$title"
    }
${if (scripts) "\n    initScripts()\n" else ""}
    APP.setupPhysicsComponents()

    runMainScene()

    app.startLoop()
}
"""
}

data class LwjglNativeDependency(
    val name: String,
    val libName: String,
    val isDefault: Boolean = false
)