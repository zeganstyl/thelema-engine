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

package app.thelema.studio.component

import app.thelema.g3d.ITransformNode
import app.thelema.studio.component.ComponentPanel.Companion.componentName
import app.thelema.studio.field.ComponentReferenceField
import app.thelema.studio.field.Mat4Widget
import app.thelema.studio.widget.Section

class BoneSection(title: String): Section(title) {
    val nodeField = ComponentReferenceField(componentName<ITransformNode>())

    val ibmSection = Section("Inverse bind matrix")
    val ibmWidget = Mat4Widget()

    init {
        content.add(nodeField).growX().newRow()

        ibmSection.content.add(ibmWidget).growX()

        content.add(ibmSection).growX().newRow()
    }
}
