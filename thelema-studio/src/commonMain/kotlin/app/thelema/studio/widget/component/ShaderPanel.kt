package app.thelema.studio.widget.component

import app.thelema.ecs.IEntityComponent
import app.thelema.shader.IShader
import app.thelema.studio.IComponentScenePanel
import app.thelema.studio.Studio
import app.thelema.studio.shader.ShaderComponentScene
import app.thelema.studio.shader.ShaderNodesDiagram
import app.thelema.ui.TextButton

class ShaderPanel: ComponentPanel<IShader>(IShader::class) {
    init {
        content.add(TextButton("Show nodes") {
            onClick {
                component?.also {
                    Studio.componentScenePanel = ShaderComponentScene as IComponentScenePanel<IEntityComponent>
                    if (Studio.scenesSplit.splits[0] > 0.95f) Studio.scenesSplit.setSplit(0, 0.5f)
                    if (ShaderComponentScene.diagram?.shader != it) {
                        ShaderComponentScene.diagram = ShaderNodesDiagram(it)
                    }
                }
            }
        }).newRow()
    }
}