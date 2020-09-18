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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.anim.AnimPlayer
import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.gltf.GLTF
import org.ksdfv.thelema.g3d.gltf.GLTFConf
import org.ksdfv.thelema.g3d.light.DirectionalLight
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.g3d.node.Node
import org.ksdfv.thelema.math.Vec3

/** Loads model for tests
 *
 * @author zeganstyl */
class GLTFModel(
    var rotate: Boolean = true,
    val conf: GLTFConf = GLTFConf().apply { separateThread = false },
    val response: (model: GLTFModel) -> Unit = {}
) {
    var rotationY = 0f
    val root = Node()

    var scene: IScene? = null
    var player: AnimPlayer? = null

    val gltf = GLTF(FS.internal("nightshade/nightshade.gltf"))

    val light = DirectionalLight().apply {
        lightIntensity = 1f
    }

    init {
        ActiveCamera.proxy = Camera().apply {
            lookAt(Vec3(1.5f, 1.8f, 1.5f), Vec3(0f, 1.15f, 0f))
            near = 0.1f
            far = 100f
            update()
        }

        gltf.conf = conf
        gltf.load {
            player = AnimPlayer()

            val nodes = ArrayList<ITransformNode>()
            gltf.nodes.forEach { nodes.add(it.node) }
            player?.nodes = nodes

            player?.setAnimation(gltf.animations[0].anim, -1)

            scene = gltf.scenes[0].scene
            scene?.lights?.add(light)

            root.addChildren(scene!!.nodes)

            response(this)
        }
    }

    fun update(delta: Float = APP.deltaTime) {
        if (rotate) {
            root.rotation.setQuaternionByAxis(0f, 1f, 0f, rotationY)
            rotationY += delta
        }

        player?.update(delta)
        scene?.update(delta)
        root.updateTransform()
    }

    fun render() {
        scene?.render()
    }
}