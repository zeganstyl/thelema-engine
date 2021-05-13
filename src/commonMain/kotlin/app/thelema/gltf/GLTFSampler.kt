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

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-sampler)
 * @author zeganstyl */
class GLTFSampler(array: IGLTFArray) : GLTFArrayElementAdapter(array) {
    var minFilter: Int = GL_LINEAR
    var magFilter: Int = GL_LINEAR
    var wrapS: Int = GL_REPEAT
    var wrapT: Int = GL_REPEAT

    override var name: String = ""

    override fun readJson() {
        super.readJson()

        minFilter = json.int("minFilter", GL_LINEAR)
        magFilter = json.int("magFilter", GL_LINEAR)
        wrapS = json.int("wrapS", GL_REPEAT)
        wrapT = json.int("wrapT", GL_REPEAT)

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        if (magFilter == GL_LINEAR || magFilter == GL_NEAREST) {
            json["magFilter"] = magFilter
        }

        when (minFilter) {
            GL_LINEAR,
            GL_NEAREST,
            GL_NEAREST_MIPMAP_NEAREST,
            GL_LINEAR_MIPMAP_NEAREST,
            GL_NEAREST_MIPMAP_LINEAR,
            GL_LINEAR_MIPMAP_LINEAR -> json["minFilter"] = minFilter
        }

        if (wrapS != GL_REPEAT) json["wrapS"] = wrapS
        if (wrapT != GL_REPEAT) json["wrapT"] = wrapT
    }

    override fun destroy() {}
}