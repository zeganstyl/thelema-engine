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

import app.thelema.ecs.component
import app.thelema.ecs.sibling
import app.thelema.gl.*
import app.thelema.img.ITexture2D

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-texture)
 *
 * @author zeganstyl */
class GLTFTexture(override val array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var sampler: Int = -1
    var source: Int = -1

    override val defaultName: String
        get() = "Texture"

    val texture: ITexture2D = gltf.texturesEntity.entity("${defaultName}_$elementIndex").component()

    override fun readJson() {
        super.readJson()

        var minFilter = gltf.conf.defaultTextureMinFilter
        var magFilter = gltf.conf.defaultTextureMagFilter
        var sWrap = GL_REPEAT
        var tWrap = GL_REPEAT

        json.int("sampler") {
            sampler = it
            val sampler = gltf.samplers[it] as GLTFSampler
            minFilter = sampler.minFilter
            magFilter = sampler.magFilter
            sWrap = sampler.wrapS
            tWrap = sampler.wrapT
        }

        val generateMipmaps = when (minFilter) {
            GL_LINEAR_MIPMAP_LINEAR,
            GL_NEAREST_MIPMAP_NEAREST,
            GL_LINEAR_MIPMAP_NEAREST,
            GL_NEAREST_MIPMAP_LINEAR -> true
            else -> false
        }

        json.int("source") { sourceIndex ->
            source = sourceIndex

            gltf.images.getOrWait(source) { image ->
                image as GLTFImage
                texture.image = image.image
                gltf.runGLCall {
                    texture.bind()
                    texture.minFilter = minFilter
                    texture.magFilter = magFilter
                    texture.sWrap = sWrap
                    texture.tWrap = tWrap
                    if (generateMipmaps) texture.generateMipmapsGPU()
                }
            }

            //json.int("texCoord") { texture.index = it }

            json.get("extensions") {
                get("KHR_texture_transform") {
                    get("offset") {
//                    textureDescriptor.offsetU = float(0)
//                    textureDescriptor.offsetV = float(1)
                    }

                    get("scale") {
//                    textureDescriptor.scaleU = float(0)
//                    textureDescriptor.scaleV = float(1)
                    }

//                float("rotation") { textureDescriptor.rotationUV = it }
//
//                int("texCoord") { textureDescriptor.uvIndex = it }
                }
            }
        }

        ready()
    }

    override fun destroy() {
        texture.destroy()
    }
}