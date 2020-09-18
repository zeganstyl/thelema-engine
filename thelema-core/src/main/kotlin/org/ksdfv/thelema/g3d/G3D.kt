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

import org.ksdfv.thelema.anim.Anim
import org.ksdfv.thelema.anim.IAnim
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.ICamera
import org.ksdfv.thelema.g3d.light.DirectionalLight
import org.ksdfv.thelema.g3d.light.ILight
import org.ksdfv.thelema.g3d.light.LightType
import org.ksdfv.thelema.g3d.node.ITransformNode
import org.ksdfv.thelema.g3d.node.Node
import org.ksdfv.thelema.kx.ThreadLocal

/** Provides default constructors for 3d graphics objects. These constructs are used by engine internally. */
@ThreadLocal
object G3D: I3DGraphicsProvider {
    override fun material(): IMaterial = Material()
    override fun scene(): IScene = Scene()
    override fun armature(): IArmature = Armature()
    override fun object3d(): IObject3D = Object3D()
    override fun node(nodeType: Int): ITransformNode = Node()
    override fun light(lightType: Int): ILight = when (lightType) {
        LightType.Directional -> DirectionalLight()
        LightType.Point -> DirectionalLight()
        LightType.Spot -> DirectionalLight()
        else -> throw NotImplementedError()
    }
    override fun camera(): ICamera = Camera()
    override fun anim(): IAnim = Anim()
}
