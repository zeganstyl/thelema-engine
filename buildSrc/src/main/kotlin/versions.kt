const val thelema_group = "app.thelema"
const val thelema_version = "0.9.0"

const val kotlin_version = "1.6.20-RC"
const val ktor_version = "1.6.7"
const val atomicfu_version = "0.15.0"

const val lwjgl_version = "3.3.0"
const val lwjgl_prefix = "org.lwjgl:lwjgl"

val lwjglNatives = arrayOf(
    "natives-linux",
    "natives-windows",
    "natives-windows-x86",
    "natives-macos",
    "natives-macos-arm64"
)

val lwjglApis = arrayOf(
    "$lwjgl_prefix:$lwjgl_version",
    "$lwjgl_prefix-glfw:$lwjgl_version",
    "$lwjgl_prefix-jemalloc:$lwjgl_version",
    "$lwjgl_prefix-openal:$lwjgl_version",
    "$lwjgl_prefix-opengl:$lwjgl_version",
    "$lwjgl_prefix-stb:$lwjgl_version"
)

val lwjglImplementations = Array(lwjglNatives.size * lwjglApis.size) {
    "${lwjglApis[it / lwjglNatives.size]}:${lwjglNatives[it % lwjglNatives.size]}"
}