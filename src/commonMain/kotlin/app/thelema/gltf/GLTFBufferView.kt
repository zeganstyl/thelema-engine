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

import app.thelema.data.IByteData

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-bufferview)
 *
 * @author zeganstyl */
class GLTFBufferView(array: IGLTFArray) : GLTFArrayElementAdapter(array) {
    var buffer: Int = -1
    var byteLength: Int = -1
    var byteOffset: Int = 0
    var byteStride: Int = -1
    var target: Int = -1

    fun createByteView(): IByteData {
        val buffer = gltf.buffers[buffer] as GLTFBuffer
        buffer.bytes.limit = byteOffset + byteLength
        buffer.bytes.position = byteOffset
        return buffer.bytes
    }

    override fun readJson() {
        super.readJson()

        buffer = json.int("buffer")
        byteLength = json.int("byteLength")
        byteOffset = json.int("byteOffset", 0)
        byteStride = json.int("byteStride", -1)
        target = json.int("target", -1)

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        json["buffer"] = buffer
        json["byteLength"] = byteLength
        if (byteOffset > 0) json["byteOffset"] = byteOffset
        if (byteStride >= 4) json["byteStride"] = byteStride
        if (target > -1) json["target"] = target
    }

    override fun destroy() {}
}
