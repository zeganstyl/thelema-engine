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

import app.thelema.data.DATA
import app.thelema.img.IImageData
import app.thelema.img.IMG
import app.thelema.utils.LOG

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-image)
 *
 * @author zeganstyl */
class GLTFImage(
    array: IGLTFArray,
    var image: IImageData = IMG.image()
): GLTFArrayElementAdapter(array) {
    var uri: String = ""
    var mimeType: String = ""
    var bufferView: Int = -1

    override val defaultName: String
        get() = "Image"

    override fun readJson() {
        super.readJson()

        uri = json.string("uri", "")
        mimeType = json.string("mimeType", "")
        bufferView = json.int("bufferView", -1)

        if (uri.isNotEmpty() && bufferView >= 0) {
            LOG.error("GLTFImage: uri and bufferView have been set, there must be only uri or only bufferView")
            gltf.images.ready(elementIndex)
            return
        }
        if (uri.isEmpty() && bufferView < 0) {
            LOG.error("GLTFImage: uri or bufferView must be set")
            gltf.images.ready(elementIndex)
            return
        }
        if (bufferView >= 0) {
            val view = gltf.bufferViews[bufferView] as GLTFBufferView
            gltf.buffers.getOrWait(view.buffer) {
                IMG.load(
                    data = view.createByteView().byteView(),
                    mimeType = mimeType,
                    out = image,
                    ready = { ready() },
                    error = {
                        LOG.error("Can't load image (bufferView = $bufferView), status: $it")
                        ready()
                    }
                )
            }
        }
        if (uri.isNotEmpty()) {
            if (uri.startsWith("data:")) {
                throw RuntimeException("GLTFLoader: uri as image data is not supported yet")
            } else {
                val decoded = DATA.decodeURI(uri)

                val file = gltf.directory.child(decoded)
                IMG.load(
                    file = file,
                    out = image,
                    ready = { ready() },
                    error = {
                        LOG.error("Can't load image ${file.path}, status: $it")
                        ready()
                    }
                )
            }
        }
    }

    override fun writeJson() {
        super.writeJson()

        if (uri.isNotEmpty()) json["uri"] = uri
        if (mimeType.isNotEmpty()) json["mimeType"] = mimeType
        if (bufferView != -1) json["bufferView"] = bufferView
    }

    override fun destroy() {
        image.destroy()
    }
}