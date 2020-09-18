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

import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.img.Texture2D
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-texture)
 *
 * @author zeganstyl */
class GLTFTexture(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var texture: Texture2D = Texture2D(textureHandle = -1),
    override var name: String = ""
): IJsonObjectIO, IGLTFArrayElement {
    var sampler: Int = -1
    var source: Int = -1

    override fun read(json: IJsonObject) {
        var minFilter = gltf.conf.defaultTextureMinFilter
        var magFilter = gltf.conf.defaultTextureMagFilter
        var sWrap = GL_REPEAT
        var tWrap = GL_REPEAT

        json.int("sampler") {
            sampler = it
            val sampler = gltf.samplers[it]
            minFilter = sampler.minFilter
            magFilter = sampler.magFilter
            sWrap = sampler.wrapS
            tWrap = sampler.wrapT
        }

        val generateMipmaps = when (minFilter) {
            GL_NEAREST, GL_LINEAR -> false

            GL_LINEAR_MIPMAP_LINEAR,
            GL_NEAREST_MIPMAP_NEAREST,
            GL_LINEAR_MIPMAP_NEAREST,
            GL_NEAREST_MIPMAP_LINEAR -> true
            else -> false
        } || when (magFilter) {
            GL_NEAREST, GL_LINEAR -> false

            GL_LINEAR_MIPMAP_LINEAR,
            GL_NEAREST_MIPMAP_NEAREST,
            GL_LINEAR_MIPMAP_NEAREST,
            GL_NEAREST_MIPMAP_LINEAR -> true
            else -> false
        }

        json.int("source") { sourceIndex ->
            source = sourceIndex

            gltf.images.getOrWait(source) { image ->
                texture.name = image.name

                if (gltf.conf.separateThread) {
                    GL.call {
                        texture.textureHandle = GL.glGenTexture()
                        texture.load(image.image, minFilter, magFilter, sWrap, tWrap, generateMipmaps = generateMipmaps)
                        gltf.textures.ready(elementIndex)
                    }
                } else {
                    texture.textureHandle = GL.glGenTexture()
                    texture.load(image.image, minFilter, magFilter, sWrap, tWrap, generateMipmaps = generateMipmaps)
                    gltf.textures.ready(elementIndex)
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
    }

    override fun write(json: IJsonObject) {
        val texture = texture

        if (texture.name.isNotEmpty()) json["name"] = texture.name
        if (sampler >= 0) json["sampler"] = sampler
        if (source >= 0) json["source"] = source
    }

    override fun destroy() {
        texture.destroy()
    }
}