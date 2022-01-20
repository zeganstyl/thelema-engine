package app.thelema.test.shader

import app.thelema.ecs.mainEntity
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.sphereMesh
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test

class TBNCalculationInShaderTest: Test {
    override fun testMain() = mainEntity {
        orbitCameraControl()

        val tmp = Vec3()

        entity("sphere") {
            sphereMesh {
                builder.uvs = false
                builder.normals = false
                builder.tangents = false
                setSize(1f)
            }

            material {
                shader = Shader(
                    vertCode = """
attribute vec3 POSITION;
varying vec3 pos;

uniform mat4 viewProj;

void main() {
    pos = POSITION;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}
""",
                    fragCode = """
varying vec3 pos;

uniform vec3 lightDirection;

void main() {
    vec3 tangent = dFdx(pos);
    vec3 bitangent = dFdy(pos);
    vec3 normal = normalize(cross(tangent, bitangent));
    mat3 tbn = mat3(tangent, bitangent, normal);

    vec3 color = vec3(dot(lightDirection, normal) * 0.5);
    
    gl_FragColor = vec4(color, 1.0);
}
"""
                ).also {
                    it.onPrepareShader = { _, _ ->
                        it["viewProj"] = ActiveCamera.viewProjectionMatrix
                        it["lightDirection"] = tmp.set(ActiveCamera.node.worldPosition).nor()
                    }
                }
            }
        }
    }
}