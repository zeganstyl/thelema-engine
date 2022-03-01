package app.thelema.gl

/** Default vertex attributes */
object Vertex {
    /** Default vertex layout for meshes and shaders */
    val Layout = VertexLayout()

    val POSITION = Layout.define("POSITION", 3)
    val TEXCOORD_0 = Layout.define("TEXCOORD_0", 2)
    val NORMAL = Layout.define("NORMAL", 3)
    val TANGENT = Layout.define("TANGENT", 4)
    val JOINTS_0 = Layout.define("JOINTS_0", 4, GL_UNSIGNED_BYTE, false)
    val WEIGHTS_0 = Layout.define("WEIGHTS_0", 4)
    val INSTANCE_POSITION = Layout.define("INSTANCE_POSITION", 3).apply { divisor = 1 }
    val COLOR = Layout.define("COLOR", 4)
}