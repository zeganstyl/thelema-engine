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

package org.ksdfv.thelema.g3d

import org.ksdfv.thelema.anim.IAnim
import org.ksdfv.thelema.g3d.cam.ICamera
import org.ksdfv.thelema.g3d.light.ILight
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.g3d.node.TransformNodeType

interface I3DGraphicsProvider {
    fun material(): IMaterial
    fun scene(): IScene
    fun armature(): IArmature
    fun object3d(): IObject3D
    fun node(nodeType: Int = TransformNodeType.TRS): ITransformNode
    fun light(lightType: Int): ILight
    fun camera(): ICamera
    fun anim(): IAnim
}