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

import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.gl.IMesh
import app.thelema.gl.IVertexBuffer
import app.thelema.json.IJsonObject

/** [glTF 2.0 specification - mesh](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#meshes)
 *
 * @author zeganstyl */
class GLTFMesh(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    val primitives = GLTFArray("primitives", array.gltf) { GLTFPrimitive(this) }

    /** Key - attribute accessor index */
    val buffersMap = HashMap<Int, IVertexBuffer>()

    /** Key - position attribute accessor index */
    val tangentsMap = HashMap<Int, IVertexBuffer>()

    /** Key - position attribute accessor index */
    val normalsMap = HashMap<Int, IVertexBuffer>()

    override val defaultName: String
        get() = "PrimitiveGroup"

    val entity: IEntity
        get() = gltf.meshesEntity.entity(name)

    fun writeEntity(entity: IEntity) {
        entity.component<ITransformNode> {
            if (gltf.conf.setupVelocityShader) enablePreviousMatrix()
        }

        for (i in primitives.indices) {
            val primitive = primitives[i] as GLTFPrimitive
            entity.addEntity(Entity {
                name = primitive.name
                component<IMesh>().inheritedMesh = primitive.mesh
            })
        }
    }

    override fun setJson(json: IJsonObject) {
        super.setJson(json)
        primitives.setJson(json.array(primitives.name))
    }

    override fun initProgress() {
        super.initProgress()
        primitives.initProgress()
        maxProgress += primitives.maxProgress
    }

    override fun updateProgress() {
        super.updateProgress()
        primitives.updateProgress()
        currentProgress = primitives.currentProgress
        if (currentProgress == maxProgress - 1) {
            currentProgress++
        }
        //println("$name: $currentProgress / $maxProgress")
    }

    override fun readJson() {
        super.readJson()

        primitives.readJson()
        ready()
    }

    override fun writeJson() {
        super.writeJson()

        json.setArray("primitives") {
            for (i in primitives.indices) {
                add(primitives[i].json)
            }
        }
    }

    override fun destroy() {
        buffersMap.clear()
        tangentsMap.clear()
        normalsMap.clear()
        primitives.destroy()
    }
}
