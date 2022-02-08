package app.thelema.studio.component

import app.thelema.g3d.mesh.MeshVisualizer
import app.thelema.gl.IMesh
import app.thelema.gl.normals
import app.thelema.gl.positions
import app.thelema.gl.tangents
import app.thelema.studio.ecs.StudioComponentSystemLayer
import app.thelema.studio.widget.PlainCheckBox
import app.thelema.studio.widget.Section
import app.thelema.ui.HBox
import app.thelema.ui.Label
import app.thelema.ui.TextButton
import app.thelema.utils.Color

class MeshPanel: ComponentPanel<IMesh>(IMesh::class) {
    val debug = MeshVisualizer()

    val normals = PlainCheckBox()
    val tangents = PlainCheckBox()

    var debugMode = false
        set(value) {
            field = value
            show.text = if (value) {
                StudioComponentSystemLayer.meshVisualizers.add(debug)
                "Hide"
            } else {
                StudioComponentSystemLayer.meshVisualizers.remove(debug)
                "Show"
            }
        }

    val show = TextButton("Show") {
        onClick {
            debugMode = !debugMode
            debug.reset()
            component?.also { mesh ->
                if (normals.isChecked) debug.addVectors3D(mesh.positions(), mesh.normals()!!, Color.BLUE)
                if (tangents.isChecked) {
                    mesh.tangents()?.also { debug.addVectors3D(mesh.positions(), it, Color.RED) }
                }
            }
        }
    }

    override var component: IMesh?
        get() = super.component
        set(value) {
            super.component = value
            debug.mesh.node = value?.node
            debugMode = false
        }

    init {
        content.add(
            Section("Debug").apply {
                content.add(HBox {
                    add(tangents).padRight(10f)
                    add(Label("Tangents"))
                }).padBottom(10f).newRow()

                content.add(HBox {
                    add(normals).padRight(10f)
                    add(Label("Normals"))
                }).padBottom(10f).newRow()

                content.add(show).newRow()
            }
        ).padLeft(10f).growX().newRow()
    }
}