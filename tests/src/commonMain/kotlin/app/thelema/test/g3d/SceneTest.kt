package app.thelema.test.g3d

import app.thelema.ecs.mainEntity
import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.scene
import app.thelema.gl.meshInstance
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class SceneTest: Test {
    override fun testMain() = mainEntity {
        scene()

        val box = boxMesh()

        entity("box1") {
            meshInstance { mesh = box.mesh }
        }

        entity("box2") {
            meshInstance { mesh = box.mesh }
        }
    }
}
