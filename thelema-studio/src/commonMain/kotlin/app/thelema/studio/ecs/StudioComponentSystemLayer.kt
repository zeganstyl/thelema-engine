package app.thelema.studio.ecs;

import app.thelema.ecs.*
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.mesh.MeshVisualizer
import app.thelema.gl.GL
import app.thelema.gl.IMeshInstance
import app.thelema.img.texture2D
import app.thelema.math.Vec3
import app.thelema.phys.IBoxShape
import app.thelema.phys.ICylinderShape
import app.thelema.phys.IRayShape
import app.thelema.phys.ISphereShape
import app.thelema.res.AID
import app.thelema.studio.Studio
import app.thelema.studio.g3d.*
import app.thelema.ui.HeadUpDisplay
import app.thelema.utils.iterate

class StudioComponentSystemLayer(val hud: HeadUpDisplay): IComponentSystemLayer {
    val grid = SceneGrid()

    val shapeRenderTool = ShapeRenderTool()

    val boxShapes = ArrayList<IBoxShape>()
    val sphereShapes = ArrayList<ISphereShape>()
    val cylinderShapes = ArrayList<ICylinderShape>()
    val directionalLights = ArrayList<DirectionalLight>()
    val rayShapes = ArrayList<IRayShape>()

    val basis = TransformBasisView()

    val lines = LineView()

    val iconTool = IconRenderTool()

    val sun = AID.texture2D("icons/7124168_sun_icon.png")

    val entityListener = object : EntityListener {
        override fun addedComponentToBranch(component: IEntityComponent) {
            if (component is IBoxShape) boxShapes.add(component)
            if (component is ISphereShape) sphereShapes.add(component)
            if (component is ICylinderShape) cylinderShapes.add(component)
            if (component is DirectionalLight) directionalLights.add(component)
            if (component is IRayShape) rayShapes.add(component)
        }

        override fun removedComponentFromBranch(component: IEntityComponent) {
            if (component is IBoxShape) boxShapes.remove(component)
            if (component is ISphereShape) sphereShapes.remove(component)
            if (component is ICylinderShape) cylinderShapes.remove(component)
            if (component is DirectionalLight) directionalLights.remove(component)
            if (component is IRayShape) rayShapes.remove(component)
        }
    }

    override fun update(delta: Float) {
        hud.update(delta)
    }

    val tmp = Vec3()

    override fun render(shaderChannel: String?) {
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.glViewport(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)

        shapeRenderTool.color.setColor(0x008000FF)
        shapeRenderTool.setupShaderCommonData()

        val isDepthTestEnabled = GL.isDepthTestEnabled
        GL.isDepthTestEnabled = false
        boxShapes.iterate { shapeRenderTool.renderBox(it) }
        sphereShapes.iterate { shapeRenderTool.renderSphere(it) }
        cylinderShapes.iterate { shapeRenderTool.renderCylinder(it) }
        rayShapes.iterate { lines.renderRay(it.position, tmp.set(it.direction).add(it.directionOffset), it.length) }
        GL.isDepthTestEnabled = isDepthTestEnabled

        meshVisualizers.iterate { it.render() }

        grid.render()

        iconTool.prepareShader()
        directionalLights.iterate {
            val pos = it.node.worldPosition
            iconTool.render(sun, pos.x, pos.y, pos.z)
        }

        Studio.activeEntityTab?.scene?.apply {
            if (translationGizmo.isEnabled) {
                translationGizmo.render()
            } else {
                selection.iterate {
                    it as EntityTreeNode
                    val matrix = it.entity.componentOrNull<ITransformNode>()?.worldMatrix
                        ?: it.entity.componentOrNull<IMeshInstance>()?.worldMatrix

                    matrix?.also { basis.render(it) }
                }
            }
        }

        Selection3D.prepareSelection()
        Selection3D.render()

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
