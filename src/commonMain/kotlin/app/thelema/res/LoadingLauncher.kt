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

package app.thelema.res

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.json.IJsonObject

class LoadingLauncher: IEntityComponent {
    override val componentName: String
        get() = "LoadingLauncher"

    override var entityOrNull: IEntity? = null

    var loader: String = ""

    var uri: String = ""

    /** If true, loading will be launched after this component reads json */
    var launchOnInit: Boolean = true

    fun launch() {
        if (uri.isNotEmpty()) {
            RES.load(uri, loader)
        }
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        if (launchOnInit) launch()
    }
}
