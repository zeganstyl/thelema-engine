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
import org.ksdfv.thelema.data.DataByteOrder
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.utils.LOG

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#buffers-and-buffer-views)
 * @author zeganstyl */
class GLTFBuffer(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var uri: String = "",
    var bytes: IByteData = DATA.bytes(0),
    var byteLength: Int = bytes.size,
    override var name: String = ""
): IJsonObjectIO, IGLTFArrayElement {
    init {
        gltf.buffers.add(this)
    }

    override fun read(json: IJsonObject) {
        json.string("uri") { uri = it }
        json.int("byteLength") { byteLength = it }

        bytes = DATA.bytes(byteLength)
        bytes.order = DataByteOrder.LittleEndian
        if (uri.startsWith("data:")) {
            // data:application/octet-stream;base64,
            val headerBody = uri.split(",".toRegex(), 2).toTypedArray()
            // System.out.println(header);
            val body = headerBody[1]
            DATA.decodeBase64(body, bytes)
        } else {
            val file = gltf.directory.child(DATA.decodeURI(uri))
            file.readBytes { status, bytes ->
                if (NET.isSuccess(status)) {
                    this.bytes.put(bytes)
                } else {
                    LOG.info("can't read $uri, status: $status")
                }

                gltf.buffers.ready(elementIndex)
            }
        }
    }

    override fun write(json: IJsonObject) {
        json["uri"] = uri
        json["byteLength"] = bytes.size
    }

    override fun destroy() {}

    fun writeBytes() {
        val file = gltf.directory.child(uri)
        bytes.position = 0
        file.writeBytes(bytes)
    }
}