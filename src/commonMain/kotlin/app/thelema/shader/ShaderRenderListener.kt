package app.thelema.shader

interface ShaderRenderListener {
    fun bind(shader: IShader) {}

    fun draw(shader: IShader) {}
}