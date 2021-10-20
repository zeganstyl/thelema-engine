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

package app.thelema.studio.widget.component

import app.thelema.res.ILoader
import app.thelema.ui.TextButton

class LoaderPanel: ComponentPanel<ILoader>(componentName<ILoader>()) {
    init {
        content.add(TextButton("Load") {
            onClick {
                component?.load()
            }
        }).newRow()
        content.add(TextButton("Reload") {
            onClick {
                component?.reload()
            }
        }).newRow()
        content.add(TextButton("Stop") {
            onClick {
                component?.stop(503)
            }
        }).newRow()
        content.add(TextButton("Destroy") {
            onClick {
                component?.destroy()
            }
        }).newRow()
    }
}