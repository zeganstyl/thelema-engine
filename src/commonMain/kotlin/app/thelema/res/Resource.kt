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
import app.thelema.fs.FS
import app.thelema.fs.IFile

class Resource: IEntityComponent {
    override var entityOrNull: IEntity? = null

    var uri: String = ""
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                file = FS.internal(uri)
                RES.resourceUriChanged(oldValue, value)
            }
        }

    override val componentName: String
        get() = "Resource"

    var loaderOrNull: ILoader? = null

    var file: IFile = FS.internal("")
}
