package app.thelema.gl

import app.thelema.concurrency.ATOM

object Vertex {
    private val idCounter = ATOM.int(0)

    private val _attributes = HashMap<String, IVertexAttribute>()
    val attributes: Map<String, IVertexAttribute>
        get() = _attributes

    val POSITION = define("POSITION", 3)
    val POSITION2D = define("POSITION2D", 2)
    val COLOR = define("COLOR", 4, GL_UNSIGNED_BYTE, true)
    val TEXCOORD_0 = define("TEXCOORD_0", 2)
    val NORMAL = define("NORMAL", 3)
    val TANGENT = define("TANGENT", 4)
    val JOINTS_0 = define("JOINTS_0", 4)
    val WEIGHTS_0 = define("WEIGHTS_0", 4)

    val INSTANCE_POSITION = define("INSTANCE_POSITION", 3).apply { divisor = 1 }

    fun newId(): Int = idCounter.getAndIncrement()

    /** Define vertex attribute
     * @param size components number */
    fun define(name: String, size: Int, type: Int, normalized: Boolean): IVertexAttribute {
        val attribute = VertexAttribute(size, name, type, normalized)
        _attributes[name] = attribute
        return attribute
    }

    /** Define float vertex attribute
     * @param size components number */
    fun define(name: String, size: Int): IVertexAttribute {
        val attribute = VertexAttribute(size, name, GL_FLOAT, false)
        _attributes[name] = attribute
        return attribute
    }
}