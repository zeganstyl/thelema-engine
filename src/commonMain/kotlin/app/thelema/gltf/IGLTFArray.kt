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

import app.thelema.json.IJsonArray
import app.thelema.json.IJsonObject
import app.thelema.utils.IAsyncList

/** All gltf-objects like skins and textures are contained in arrays
 *
 * @author zeganstyl */
interface IGLTFArray: IAsyncList<IGLTFArrayElement> {
    val name: String

    val gltf: IGLTF

    val progress: Float
        get() = currentProgress.toFloat() / maxProgress

    val currentProgress: Int
    val maxProgress: Int

    var jsonOrNull: IJsonArray?
    val json: IJsonArray
        get() = jsonOrNull!!

    fun setJson(json: IJsonArray) {
        jsonOrNull = json
        json.forEachObject {
            addElement(this)
        }
    }

    fun initProgress() {
        for (i in indices) {
            get(i).initProgress()
        }
    }

    fun updateProgress()

    fun readJson() {
        var i = 0
        json.forEachObject {
            get(i).readJson()
            i++
        }
    }

    fun writeJson() {
        for (i in indices) {
            json.add(get(i).json)
        }
    }

    fun addElement(): IGLTFArrayElement

    fun addElement(json: IJsonObject): IGLTFArrayElement {
        val element = addElement()
        element.setJson(json)
        return element
    }

    fun destroy() {
        for (i in indices) {
            get(i).destroy()
        }
        clear()
    }
}
