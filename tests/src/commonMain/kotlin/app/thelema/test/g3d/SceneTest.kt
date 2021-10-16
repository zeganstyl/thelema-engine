package app.thelema.test.g3d

import app.thelema.ecs.mainEntity
import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.scene
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class SceneTest: Test {
    override fun testMain() {
        mainEntity {
            scene()
            material { shader = SimpleShader3D() }
            boxMesh(2f)
        }
    }
}
