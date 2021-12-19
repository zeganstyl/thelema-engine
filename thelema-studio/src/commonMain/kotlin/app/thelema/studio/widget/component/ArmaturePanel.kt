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

import app.thelema.g3d.IArmature
import app.thelema.g3d.ITransformNode
import app.thelema.studio.widget.Section

class ArmaturePanel: ComponentPanel<IArmature>(componentName<IArmature>()) {
    override var component: IArmature?
        get() = super.component
        set(value) {
            super.component = value
            bonesSection.content.clearChildren()
            if (value != null) {
                for (i in value.bones.indices) {
                    val boneSection = BoneSection("[$i]")
                    boneSection.nodeField.set = { value.setBone(i, it as ITransformNode?) }
                    boneSection.nodeField.get = { value.bones[i] }
                    boneSection.ibmWidget.set = { value.inverseBindMatrices[i].set(it) }
                    boneSection.ibmWidget.get = { value.inverseBindMatrices[i] }
                    bonesSection.content.add(boneSection).growX().newRow()
                }
            }
        }

    val bonesSection = Section("Bones")

    init {
        content.add(bonesSection).growX().newRow()
    }
}
