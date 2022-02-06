package app.thelema.studio.widget.component

import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class OrbitCameraControlPanel: ComponentPanel<OrbitCameraControl>(OrbitCameraControl::class) {
    init {
        content.add(TextButton("Position from active camera") {
            onClick {
                component?.also { component ->
                    Studio.tabsPane.activeTab?.scene?.cameraControl?.also { other ->
                        component.zenith = other.zenith
                        component.azimuth = other.azimuth
                        component.targetDistance = other.targetDistance
                        component.target = other.target
                    }
                }
            }
        })
    }
}