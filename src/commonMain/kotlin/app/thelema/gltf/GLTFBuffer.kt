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

import app.thelema.app.APP
import app.thelema.app.printElapsedTime
import app.thelema.data.DATA
import app.thelema.data.DataByteOrder
import app.thelema.data.IByteData
import app.thelema.utils.LOG

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#buffers-and-buffer-views)
 * @author zeganstyl */
class GLTFBuffer(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var uri: String = ""
    var bytes: IByteData = DATA.nullBuffer
    var byteLength: Int = bytes.limit

    override fun readJson() {
        super.readJson()

        uri = json.string("uri", "")
        byteLength = json.int("byteLength", 0)

        if (uri.isEmpty()) {
            bytes = gltf.binChunks[elementIndex].chunkData
            ready()
        } else {
            if (uri.startsWith("data:")) {
                // data:application/octet-stream;base64,
                val headerBody = uri.split(",".toRegex(), 2).toTypedArray()
                // System.out.println(header);
                val body = headerBody[1]

                bytes = DATA.decodeBase64(body)
                bytes.order = DataByteOrder.LittleEndian
            } else {
                val file = gltf.directory.child(DATA.decodeURI(uri))
                file.readBytes(
                    ready = {
                        bytes = it
                        ready()
                    },
                    error = {
                        LOG.info("can't read $uri, status: $it")
                        ready()
                    }
                )
            }
        }
    }

    override fun writeJson() {
        super.writeJson()

        json["uri"] = uri
        json["byteLength"] = bytes.limit
    }

    override fun destroy() {
        bytes.destroy()
        bytes = DATA.nullBuffer
    }

    fun writeBytes() {
        val file = gltf.directory.child(uri)
        bytes.position = 0
        file.writeBytes(bytes)
    }
}