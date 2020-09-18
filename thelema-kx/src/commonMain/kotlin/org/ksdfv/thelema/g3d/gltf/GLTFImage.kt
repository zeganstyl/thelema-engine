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

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.img.IImageData
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.net.NET

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-image)
 *
 * @author zeganstyl */
class GLTFImage(
    override val gltf: GLTF,
    override var elementIndex: Int,
    var image: IImageData = IMG.image()
): IJsonObjectIO, IGLTFArrayElement {
    override var name: String = ""
    var uri: String = ""
    var mimeType: String = ""
    var bufferView: Int = -1

    override fun read(json: IJsonObject) {
        json.string("name") { name = it }
        json.string("uri") { uri = it }
        json.string("mimeType") { mimeType = it }
        json.int("bufferView") { bufferView = it }

        if (json.contains("bufferView")) {
            val bufferView = gltf.bufferViews[json.int("bufferView")]
            val buffer = gltf.buffers[bufferView.buffer]

            buffer.bytes.position = bufferView.byteOffset
            buffer.bytes.size = bufferView.byteLength
            image.bytes = buffer.bytes.byteView()
            buffer.bytes.position = 0
            buffer.bytes.size = buffer.bytes.capacity

            gltf.images.ready(elementIndex)
        } else {
            json.string("uri") { uri ->
                if (uri.startsWith("data:")) {
                    throw RuntimeException("GltfLoader: uri as image data is not supported yet")
                } else {
                    val decoded = DATA.decodeURI(uri)

                    IMG.load(gltf.directory.child(decoded), image) { status, _ ->
                        if (NET.isSuccess(status)) {
                            gltf.images.ready(elementIndex)
                        }
                    }
                }
            }
        }
    }

    override fun write(json: IJsonObject) {
        if (name.isNotEmpty()) json["name"] = name
        if (uri.isNotEmpty()) json["uri"] = uri
        if (mimeType.isNotEmpty()) json["mimeType"] = mimeType
        if (bufferView != -1) json["bufferView"] = bufferView
    }

    override fun destroy() {
        image.destroy()
    }
}