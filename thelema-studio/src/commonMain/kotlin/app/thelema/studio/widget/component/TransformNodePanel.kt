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

import app.thelema.g3d.ITransformNode
import app.thelema.studio.widget.QuaternionWidget
import app.thelema.studio.widget.Vec3Widget
import app.thelema.studio.widget.Vec4Widget
import app.thelema.ui.TextButton

class TransformNodePanel: ComponentPanel<ITransformNode>(ITransformNode::class, true) {
    init {
        val block: (String) -> Unit = { component?.requestTransformUpdate() }
        fieldWidget<Vec3Widget>("position") {
            xField.onChanged(block)
            yField.onChanged(block)
            zField.onChanged(block)
        }
        fieldWidget<QuaternionWidget>("rotation") {
            xField.onChanged(block)
            yField.onChanged(block)
            zField.onChanged(block)
        }
        fieldWidget<Vec3Widget>("scale") {
            xField.onChanged(block)
            yField.onChanged(block)
            zField.onChanged(block)
        }

        content.add(TextButton("Update") {
            onClick {
                component?.requestTransformUpdate()
            }
        }).growX().newRow()
    }
}