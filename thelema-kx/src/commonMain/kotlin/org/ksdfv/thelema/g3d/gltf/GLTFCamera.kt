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

package org.ksdfv.thelema.g3d.gltf

import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.ICamera
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-camera)
 *
 * @author zeganstyl */
class GLTFCamera(
    override val gltf: IGLTF,
    override var elementIndex: Int,
    var camera: ICamera = Camera()
): IJsonObjectIO, IGLTFArrayElement {
    override var name: String = ""
    var type: String = ""

    var aspectRatio: Float = 0f
    var yfov: Float = 0f

    var zfar: Float = 0f
    var znear: Float = 0f

    var xmag: Float = 0f
    var ymag: Float = 0f

    override fun read(json: IJsonObject) {
        json.string("name") { name = it }
        type = json.string("type")

        json.obj("perspective") {
            json.float("aspectRatio") { aspectRatio = it }
            yfov = json.float("yfov")
            json.float("zfar") {
                zfar = it
                camera.far = it
            }
            json.float("znear") {
                znear = it
                camera.near = it
            }
        }

        json.obj("orthographic") {
            json.float("xmag") { xmag = it }
            json.float("ymag") { ymag = it }
            json.float("zfar") {
                zfar = it
                camera.far = it
            }
            json.float("znear") {
                znear = it
                camera.near = it
            }
        }

        gltf.cameras.ready(elementIndex)
    }

    override fun write(json: IJsonObject) {
        json["type"] = type
        when (type) {
            "perspective" -> {
                json.set("perspective") {
                    if (aspectRatio != 0f) set("aspectRatio", aspectRatio)
                    set("yfov", yfov)
                    if (zfar != 0f) set("zfar", zfar)
                    set("znear", znear)
                }
            }
            "orthographic" -> {
                json.set("orthographic") {
                    set("xmag", xmag)
                    set("ymag", ymag)
                    set("zfar", zfar)
                    set("znear", znear)
                }
            }
        }
    }

    override fun destroy() {}
}