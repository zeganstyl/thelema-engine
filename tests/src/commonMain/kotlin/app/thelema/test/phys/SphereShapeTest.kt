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


import app.thelema.test.Test

/** @author zeganstyl */
class SphereShapeTest: Test {
    override val name: String
        get() = "Sphere shape"

    override fun testMain() {
//        val world = PHYS.world()
//        world.setGravity(0f, -2f, 0f)
//
//        val dynamic = world.rigidBody(world.sphereShape(1f), 1f)
//        dynamic.setPosition(0.5f, 3f, 0.5f)
//
//        val static1 = world.rigidBody(world.sphereShape(1f))
//        static1.setPosition(1f, 0f, 1.5f)
//        static1.isGravityEnabled = false
//        static1.isStatic = true
//
//        val static2 = world.rigidBody(world.sphereShape(1f))
//        static2.setPosition(-2f, 0f, 1.5f)
//        static2.isGravityEnabled = false
//        static2.isStatic = true
//
//        val static3 = world.rigidBody(world.sphereShape(1f))
//        static3.setPosition(0f, 0f, -1.5f)
//        static3.isGravityEnabled = false
//        static3.isStatic = true
//
//        val mesh = SphereMeshBuilder(1f).apply {
//            positionName = "POSITION"
//            uvName = "UV"
//        }.build()
//
//
//        val shader = Shader(
//            vertCode = """
//attribute vec3 POSITION;
//attribute vec2 UV;
//varying vec2 uv;
//uniform mat4 world;
//uniform mat4 viewProj;
//
//void main() {
//    uv = UV;
//    gl_Position = viewProj * world * vec4(POSITION, 1.0);
//}""",
//            fragCode = """
//varying vec2 uv;
//void main() {
//    gl_FragColor = vec4(uv, 0.0, 1.0);
//}"""
//        )
//
//        val control = OrbitCameraControl(
//            camera = ActiveCamera,
//            targetDistance = 10f,
//            azimuth = 2f
//        )
//        control.listenToMouse()
//        LOG.info(control.help)
//
//        val tmp = Mat4()
//
//        GL.isDepthTestEnabled = true
//        GL.glClearColor(0f, 0f, 0f, 1f)
//        GL.render {
//            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
//            control.update(APP.deltaTime)
//            ActiveCamera.updateCamera()
//
//            world.step(APP.deltaTime)
//
//            shader.bind()
//            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
//
//            shader["world"] = static1.getWorldTransform(tmp)
//            mesh.render(shader)
//
//            shader["world"] = static2.getWorldTransform(tmp)
//            mesh.render(shader)
//
//            shader["world"] = static3.getWorldTransform(tmp)
//            mesh.render(shader)
//
//            shader["world"] = dynamic.getWorldTransform(tmp)
//            mesh.render(shader)
//        }
    }
}