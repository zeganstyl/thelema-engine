package app.thelema.shader

interface ShaderListener {
    fun loaded(shader: IShader) {}

    fun bind(shader: IShader) {}

    fun draw(shader: IShader) {}
}