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

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.fs.IFile
import app.thelema.g3d.IScene
import app.thelema.res.ILoader

/** @author zeganstyl */
interface IGLTF: ILoader {
    var generator: String
    var version: String
    var copyright: String
    var minVersion: String

    var conf: GLTFConf

    /** Main scene */
    var scene: IEntity

    var mainSceneIndex: Int

    val extensions: MutableList<IGLTFExtension>

    val buffers: IGLTFArray
    val bufferViews: IGLTFArray
    val accessors: IGLTFArray
    val samplers: IGLTFArray
    val images: IGLTFArray
    val textures: IGLTFArray
    val materials: IGLTFArray
    val meshes: IGLTFArray
    val nodes: IGLTFArray
    val skins: IGLTFArray
    val animations: IGLTFArray
    val scenes: IGLTFArray
    val cameras: IGLTFArray

    val meshesEntity: IEntity
    val imagesEntity: IEntity
    val texturesEntity: IEntity
    val materialsEntity: IEntity
    val animationsEntity: IEntity
    val scenesEntity: IEntity

    val extra: MutableMap<String, Any>

    val directory: IFile

    val chunks: MutableList<GLBChunk>
    val binChunks: MutableList<GLBChunk>

    fun getArray(name: String): IGLTFArray

    fun runGLCall(block: () -> Unit)
}
