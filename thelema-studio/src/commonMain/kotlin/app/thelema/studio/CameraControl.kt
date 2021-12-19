package app.thelema.studio

import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.input.BUTTON

object CameraControl {
    val control = OrbitCameraControl {
        rotateButton = BUTTON.MIDDLE
        keyboardEnabled = false
        scrollFactor = 0.05f
        isEnabled = true
        stopListenMouse()
    }
}