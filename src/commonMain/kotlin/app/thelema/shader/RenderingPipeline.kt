package app.thelema.shader

import app.thelema.ecs.IEntity

class RenderingPipeline: IRenderingPipeline {
    override val componentName: String
        get() = "RenderingPipeline"

    override var entityOrNull: IEntity? = null

    var proxy: IRenderingPipeline? = null

    override fun setResolution(width: Int, height: Int) {
        proxy?.setResolution(width, height)
    }

    override fun render(block: (shaderChannel: String?) -> Unit) {
        proxy?.render(block)
    }
}