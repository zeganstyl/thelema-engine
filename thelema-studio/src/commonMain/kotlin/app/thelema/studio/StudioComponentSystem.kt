package app.thelema.studio;

import app.thelema.ecs.*
import app.thelema.g3d.mesh.MeshVisualizer
import app.thelema.gl.GL
import app.thelema.phys.IBoxShape
import app.thelema.phys.ISphereShape
import app.thelema.ui.HeadUpDisplay
import app.thelema.utils.iterate

class StudioComponentSystem(val hud: HeadUpDisplay): IComponentSystem {
    val shapeRenderTool = ShapeRenderTool()

    val boxShapes = ArrayList<IBoxShape>()
    val sphereShapes = ArrayList<ISphereShape>()

    val entityListener = object : EntityListener {
        override fun addedComponentToBranch(component: IEntityComponent) {
            if (component is IBoxShape) boxShapes.add(component)
            if (component is ISphereShape) sphereShapes.add(component)
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (component is IBoxShape) boxShapes.remove(component)
            if (component is ISphereShape) sphereShapes.remove(component)
        }
    }

    override fun update(delta: Float) {
        hud.update(delta)
    }

    override fun render(shaderChannel: String?) {
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.glViewport(0, 0, hud.width.toInt(), hud.height.toInt())

        shapeRenderTool.color.setColor(0x008000FF)
        boxShapes.iterate { shapeRenderTool.renderBox(it) }
        sphereShapes.iterate { shapeRenderTool.renderSphere(it) }

        meshVisualizers.iterate { it.render() }

        hud.render()
    }

    override fun addedScene(entity: IEntity) {
        entity.forEachComponentInBranch { entityListener.addedComponentToBranch(it) }
        entity.addEntityListener(entityListener)
    }

    override fun removedScene(entity: IEntity) {
        entity.forEachComponentInBranch { entityListener.removedComponentFromBranch(it) }
        entity.removeEntityListener(entityListener)
    }

    companion object {
        val meshVisualizers = ArrayList<MeshVisualizer>()
    }
}