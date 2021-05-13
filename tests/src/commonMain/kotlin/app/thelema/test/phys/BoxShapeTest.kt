/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.test.phys

import app.thelema.app.APP
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.test.Test
import app.thelema.math.Mat4
import app.thelema.phys.*
import app.thelema.shader.Shader
import app.thelema.utils.LOG

/** @author zeganstyl */
class BoxShapeTest: Test {
    override val name: String
        get() = "Box shape"

    override fun testMain() {
        val mainScene = Entity {
            component<Scene>()

            componentTyped<IRigidBodyPhysicsWorld>(IRigidBodyPhysicsWorld.Name) {
                setGravity(0f, -2f, 0f)
            }

            val box = component<BoxMesh> {
                builder.positionName = "POSITION"
                builder.uvName = "UV"
            }

            entity("dynamicBox").apply {
                componentTyped<IBoxShape>(IBoxShape.Name) { setSize(2f) }
                componentTyped<IRigidBody>(IRigidBody.Name) {
                    node.position.set(0f, 3f, 0f)
                    node.requestTransformUpdate()
                }
                component<Object3D> { addMesh(box.mesh) }
            }

            entity("staticBox1").apply {
                componentTyped<IBoxShape>(IBoxShape.Name) { setSize(2f) }
                componentTyped<IRigidBody>(IRigidBody.Name) {
                    node.position.set(1.25f, 0f, 0f)
                    node.requestTransformUpdate()
                    isGravityEnabled = false
                    isStatic = true
                }
                component<Object3D> { addMesh(box.mesh) }
            }

            entity("staticBox2").apply {
                componentTyped<IBoxShape>(IBoxShape.Name) { setSize(2f) }
                componentTyped<IRigidBody>(IRigidBody.Name) {
                    node.position.set(-2.5f, 0f, 0f)
                    node.requestTransformUpdate()
                    isGravityEnabled = false
                    isStatic = true
                }
                component<Object3D> { addMesh(box.mesh) }
            }
        }

        val boxMesh = BoxMeshBuilder(2f, 2f, 2f).apply {
            positionName = "POSITION"
            uvName = "UV"
        }.build()


        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
varying vec2 uv;
uniform mat4 world;
uniform mat4 viewProj;

void main() {
    uv = UV;
    gl_Position = viewProj * world * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec2 uv;
void main() {
    gl_FragColor = vec4(uv, 0.0, 1.0);
}"""
        )

        val control = OrbitCameraControl(
            camera = ActiveCamera,
            targetDistance = 10f,
            azimuth = 2f
        )
        control.listenToMouse()
        LOG.info(control.help)

        val tmp = Mat4()

        GL.isDepthTestEnabled = true
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

//            world.step(APP.deltaTime)
//
//            shader.bind()
//            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
//
//            shader["world"] = static1.getWorldTransform(tmp)
//            boxMesh.render(shader)
//
//            shader["world"] = static2.getWorldTransform(tmp)
//            boxMesh.render(shader)
//
//            shader["world"] = dynamic.getWorldTransform(tmp)
//            boxMesh.render(shader)
        }
    }
}