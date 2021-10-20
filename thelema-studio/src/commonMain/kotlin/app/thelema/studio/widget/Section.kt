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

package app.thelema.studio.widget

import app.thelema.ui.*

open class Section(title: String): VBox() {
    val expandButton = TextButton("+") {
        onClick {
            isExpanded = !isExpanded
        }
    }

    val titleLabel = Label(title, alignment = Align.left)
    val titleIcon = UIImage()
    val titleTable = HBox {
        align(Align.left)
        add(expandButton).width(20f)
        add(titleIcon).width(24f).pad(3f)
        add(titleLabel).growX().padLeft(5f)
    }

    val content = Table()

    var isExpanded: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                clearChildren()
                add(titleTable).height(30f).growX()
                if (value) add(content).growX().padLeft(5f)
                expandButton.text = if (value) "-" else "+"
            }
        }

    init {
        add(titleTable).height(30f).growX()
    }
}
