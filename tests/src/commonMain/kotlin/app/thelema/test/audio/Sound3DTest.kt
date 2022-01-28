package app.thelema.test.audio

import app.thelema.audio.sound
import app.thelema.ecs.mainEntity
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.mesh.planeMesh
import app.thelema.res.RES
import app.thelema.res.load
import app.thelema.test.Test

class Sound3DTest: Test {
    override fun testMain() = mainEntity {

        orbitCameraControl()

        entity("plane") {
            planeMesh { setSize(10f) }
        }

        entity("source") {
            boxMesh()
            sound {
                isLooped = true
                mustPlay = true
                soundLoader = RES.load("242501__gabrielaraujo__powerup-success.ogg")
            }
        }
    }
}