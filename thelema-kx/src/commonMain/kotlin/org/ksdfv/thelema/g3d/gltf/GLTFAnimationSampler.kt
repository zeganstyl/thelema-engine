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

import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-animation-sampler)
 *
 * @author zeganstyl */
class GLTFAnimationSampler(
    var input: Int = -1,
    var interpolation: String = "LINEAR",
    var output: Int = -1
): IJsonObjectIO {
    override fun read(json: IJsonObject) {
        input = json.int("input")
        json.string("interpolation") { interpolation = it }
        output = json.int("output")
    }

    override fun write(json: IJsonObject) {
        json["input"] = input
        if (interpolation != "LINEAR") json["interpolation"] = interpolation
        json["output"] = output
    }
}