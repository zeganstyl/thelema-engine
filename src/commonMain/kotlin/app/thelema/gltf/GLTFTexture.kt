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

import app.thelema.gl.*
import app.thelema.img.Texture2D

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-texture)
 *
 * @author zeganstyl */
class GLTFTexture(
    override val array: IGLTFArray,
    var texture: Texture2D = Texture2D(0)
): GLTFArrayElementAdapter(array) {
    var sampler: Int = -1
    var source: Int = -1

    override fun readJson() {
        super.readJson()

        texture.name = name

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

        // magFilter can't have mipmap magnification
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

                gltf.runGLCall {
                    texture.load(image.image, generateMipmaps = generateMipmaps) {
                        bind()
                        this.minFilter = minFilter
                        this.magFilter = minFilter
                        this.sWrap = sWrap
                        this.tWrap = tWrap
                    }
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

    override fun writeJson() {
        super.writeJson()

        if (sampler >= 0) json["sampler"] = sampler
        if (source >= 0) json["source"] = source
    }

    override fun destroy() {
        texture.destroy()
    }
}