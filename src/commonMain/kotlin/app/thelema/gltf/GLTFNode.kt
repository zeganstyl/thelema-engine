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

package app.thelema.gltf

import app.thelema.math.*

/** [glTF 2.0 specification - reference](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-node)
 *
 * [glTF 2.0 specification - node](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#nodes-and-hierarchy)
 *
 * @author zeganstyl */
class GLTFNode(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var camera: Int = -1
    val children: MutableList<Int> = ArrayList()
    var skin: Int = -1
    var mesh: Int = -1
    val weights: MutableList<Float> = ArrayList()
    var matrix: IMat4? = null
    val rotation: IVec4 = Vec4(0f, 0f, 0f, 1f)
    val rotationMatrix: IMat3 = Mat3()
    val scale: IVec3 = Vec3(1f, 1f, 1f)
    val translation: IVec3 = Vec3()

    override val defaultName: String
        get() = "Node"

    override fun readJson() {
        super.readJson()

        children.clear()
        json.array("children") { forEachInt { children.add(it) } }

        translation.set(0f, 0f, 0f)
        rotation.set(0f, 0f, 0f, 1f)
        scale.set(1f, 1f, 1f)
        matrix = null
        if (json.contains("matrix")) {
            val mat = Mat4()
            matrix = mat
            json.array("matrix") {
                var i = 0
                forEachFloat {
                    mat.values[i] = it
                    i++
                }
            }
            mat.getTRS(translation, rotationMatrix, scale)
        } else {
            json.array("translation") { translation.set(float(0), float(1), float(2)) }
            json.array("rotation") { rotation.set(float(0), float(1), float(2), float(3)) }
            json.array("scale") { scale.set(float(0), float(1), float(2)) }

            rotationMatrix.idt()
            rotationMatrix.rotateByQuaternion(rotation)
        }

        weights.clear()
        json.forEachFloat("weights") { weights.add(it) }

        camera = json.int("camera", -1)

        mesh = json.int("mesh", -1)

        skin = json.int("skin", -1)

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        if (name.isNotEmpty()) json["name"] = name
        if (children.isNotEmpty()) json.setInts("children", children.size) { children[it] }
        if (weights.isNotEmpty()) json.setFloats("weights", weights.size) { weights[it] }

        if (mesh != -1) json["mesh"] = mesh
        if (skin != -1) json["skin"] = skin
        if (camera != -1) json["camera"] = camera

        val matrix = matrix
        if (matrix != null) {
            json.setFloats("matrix", 16) { matrix.values[it] }
        } else {
            val t = translation
            if (t.x != 0f || t.y != 0f || t.z != 0f) json.setArray("translation") { add(t.x, t.y, t.z) }
            val r = rotation
            if (r.x != 0f || r.y != 0f || r.z != 0f || r.w != 1f) json.setArray("rotation") { add(r.x, r.y, r.z, r.w) }
            val s = scale
            if (s.x != 1f || s.y != 1f || s.z != 1f) json.setArray("scale") { add(s.x, s.y, s.z) }
        }
    }

    override fun destroy() {
        children.clear()
        matrix = null
    }
}