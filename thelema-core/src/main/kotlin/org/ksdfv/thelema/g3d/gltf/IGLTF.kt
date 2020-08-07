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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.utils.IAsyncList

/** @author zeganstyl */
interface IGLTF: IJsonObjectIO {
    var generator: String
    var version: String
    var copyright: String
    var minVersion: String

    var name: String

    var conf: GLTFConf

    val objects: IAsyncList<IObject3D>

    var scene: IScene?
    var mainSceneIndex: Int

    val arrays: MutableList<IGLTFArray<*>>

    val buffers: IGLTFArray<GLTFBuffer>
    val bufferViews: IGLTFArray<GLTFBufferView>
    val accessors: IGLTFArray<GLTFAccessor>
    val samplers: IGLTFArray<GLTFSampler>
    val images: IGLTFArray<GLTFImage>
    val textures: IGLTFArray<GLTFTexture>
    val materials: IGLTFArray<IGLTFMaterial>
    val meshes: IGLTFArray<GLTFMesh>
    val nodes: IGLTFArray<GLTFNode>
    val skins: IGLTFArray<GLTFSkin>
    val animations: IGLTFArray<GLTFAnimation>
    val scenes: IGLTFArray<GLTFScene>
    val cameras: IGLTFArray<GLTFCamera>

    val extra: MutableMap<String, Any>

    val directory: IFile

    /** Create material and add it to array */
    fun createMaterial(): IGLTFMaterial

    /** Save buffers to files, in particular .bin */
    fun saveBuffers()

    fun destroy()
}