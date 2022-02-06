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

import app.thelema.json.IJsonObject

/** @author zeganstyl */
interface IGLTFArrayElement {
    val gltf: IGLTF
    var elementIndex: Int
    var name: String

    val conf: GLTFSettings
        get() = array.gltf.conf

    val progress: Float
        get() = currentProgress.toFloat() / maxProgress

    val currentProgress: Int
    val maxProgress: Int

    var arrayOrNull: IGLTFArray?
    val array: IGLTFArray
        get() = arrayOrNull!!

    var jsonOrNull: IJsonObject?
    val json: IJsonObject
        get() = jsonOrNull!!

    fun initProgress()

    fun setJson(json: IJsonObject) {
        jsonOrNull = json
    }

    fun updateProgress()

    fun readJson()

    fun writeJson()

    fun ready() {
        array.ready(elementIndex)
    }

    fun destroy()
}
