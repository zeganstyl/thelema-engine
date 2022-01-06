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
import app.thelema.fs.IFile
import app.thelema.g3d.ISceneProvider
import app.thelema.res.ILoader

/** @author zeganstyl */
interface IGLTF: ILoader, ISceneProvider {
    var generator: String
    var version: String
    var copyright: String
    var minVersion: String

    var conf: GLTFSettings

    /** Store mesh buffers in RAM */
    var saveMeshesInCPUMem: Boolean
        get() = conf.saveMeshesInCPUMem
        set(value) { conf.saveMeshesInCPUMem = value }

    /** Velocity rendering may be used for motion blur */
    var setupVelocityShader: Boolean
        get() = conf.setupVelocityShader
        set(value) { conf.setupVelocityShader = value }

    var receiveShadows: Boolean
        get() = conf.receiveShadows
        set(value) { conf.receiveShadows = value }

    var setupDepthRendering: Boolean
        get() = conf.setupDepthRendering
        set(value) { conf.setupDepthRendering = value }

    /** Merge vertex attribute data in one VBO for each primitive */
    var mergeVertexAttributes: Boolean
        get() = conf.mergeVertexAttributes
        set(value) { conf.mergeVertexAttributes = value }

    var generateShaders: Boolean
        get() = conf.generateShaders
        set(value) { conf.generateShaders = value }

    var ibl: Boolean
        get() = conf.ibl
        set(value) { conf.ibl = value }

    var iblMaxMipLevels: Int
        get() = conf.iblMaxMipLevels
        set(value) { conf.iblMaxMipLevels = value }

    /** Setup pipeline for deferred shading */
    var setupGBufferShader: Boolean
        get() = conf.setupGBufferShader
        set(value) { conf.setupGBufferShader = value }

    /** Main scene */
    var scene: IEntity

    var mainSceneIndex: Int

    /** Override assets, except scenes */
    var overrideAssets: Boolean

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
