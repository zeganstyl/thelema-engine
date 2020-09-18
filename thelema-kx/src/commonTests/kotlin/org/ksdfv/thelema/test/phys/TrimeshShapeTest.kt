/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test.phys


import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.mesh.gen.PlaneMeshBuilder
import org.ksdfv.thelema.phys.PHYS
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG
import kotlin.random.Random

/** @author zeganstyl */
class TrimeshShapeTest: Test {
    override val name: String
        get() = "Trimesh shape"

    override fun testMain() {
        val world = PHYS.world()
        world.setGravity(0f, -2f, 0f)

        val mesh = BoxMeshBuilder(xSize = 1f, ySize = 1f, zSize = 1f).apply {
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        val plane = PlaneMeshBuilder(
            width = 20f,
            height = 20f,
            xDivisions = 5,
            yDivisions = 5,
            heightProvider = { _, _ -> Random.nextFloat() * 5f }
        ).apply {
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        val dynamic = world.rigidBody(world.boxShape(1f, 1f, 1f), 5f)
        dynamic.setPosition(0f, 5f, 0f)

        val static = world.rigidBody(world.trimeshShape(plane, "POSITION"), 0f)
        static.setPosition(0f, -3f, 0f)


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

        val camera = Camera()

        val control = OrbitCameraControl(
            camera = camera,
            targetDistance = 20f,
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
            camera.update()

            world.step(APP.deltaTime)

            shader.bind()
            shader["viewProj"] = camera.viewProjectionMatrix

            shader["world"] = static.getWorldTransform(tmp)
            plane.render(shader)

            shader["world"] = dynamic.getWorldTransform(tmp)
            mesh.render(shader)
        }
    }
}