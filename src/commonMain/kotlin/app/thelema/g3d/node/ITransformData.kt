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

package app.thelema.g3d.node

import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.math.TransformDataType

interface ITransformData: IJsonObjectIO {
    /** Local translation, relative to the parent. */
    val position: IVec3

    /** Local rotation, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val rotation: IVec4

    /** Local scale, relative to the parent. It may not work, check type of this node [TransformDataType]. */
    val scale: IVec3

    /** Global world transform, product of multiply local and parent transform data, calculated via [updateTransform]. */
    val worldMatrix: IMat4

    /** See [TransformDataType] */
    val transformDataType: String
        get() = TransformDataType.TRS

    var previousWorldMatrix: IMat4?

    fun set(other: ITransformData) {
        position.set(other.position)
        rotation.set(other.rotation)
        scale.set(other.scale)
        worldMatrix.set(other.worldMatrix)
    }

    override fun readJson(json: IJsonObject) {
        json.array("position") {
            position.x = float(0)
            position.y = float(1)
            position.z = float(2)
        }
        json.array("rotation") {
            rotation.x = float(0)
            rotation.y = float(1)
            rotation.z = float(2)
            rotation.w = float(3)
        }
        json.array("scale") {
            scale.x = float(0)
            scale.y = float(1)
            scale.z = float(2)
        }
    }

    override fun writeJson(json: IJsonObject) {
        json.setArray("position") { add(position.x, position.y, position.z) }
        json.setArray("rotation") { add(rotation.x, rotation.y, rotation.z, rotation.w) }
        json.setArray("scale") { add(scale.x, scale.y, scale.z) }
    }
}