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

abstract class GLTFArrayElementAdapter(arrayOrNull: IGLTFArray? = null): IGLTFArrayElement {
    override val gltf: IGLTF
        get() = array.gltf

    override var name: String = ""

    override val progress: Float
        get() = currentProgress.toFloat() / maxProgress

    override var currentProgress: Long = 0
    override var maxProgress: Long = 1

    protected open val defaultName: String
        get() = ""

    override var elementIndex: Int = arrayOrNull?.size ?: 0

    override var arrayOrNull: IGLTFArray? = arrayOrNull

    override var jsonOrNull: IJsonObject? = null

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    override fun updateProgress() {}

    override fun ready() {
        super.ready()
        currentProgress++
    }

    override fun setJson(json: IJsonObject) {
        super.setJson(json)
        name = json.string("name", defaultName)
    }

    override fun readJson() {}

    override fun writeJson() {
        if (name.isNotEmpty()) json["name"] = name
    }
}