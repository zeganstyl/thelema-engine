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

package app.thelema.gltf.tetf

import app.thelema.ecs.Entity
import app.thelema.gltf.GLTFArrayElementAdapter
import app.thelema.gltf.IGLTFArray

class TETFEntity(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    val entity = Entity()

    override fun readJson() {
        super.readJson()
        entity.readJson(json)
    }

    override fun writeJson() {
        super.writeJson()
        entity.writeJson(json)
    }

    override fun destroy() {
        entity.destroy()
    }
}
