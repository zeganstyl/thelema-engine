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

package app.thelema.gltf

import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.ICamera
import app.thelema.utils.LOG

/** [glTF 2.0 specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-camera)
 *
 * @author zeganstyl */
class GLTFCamera(
    array: IGLTFArray,
    var camera: ICamera = Camera()
): GLTFArrayElementAdapter(array) {
    var type: String = ""

    var aspectRatio: Float = 0f
    var yfov: Float = 0f

    var zfar: Float = 0f
    var znear: Float = 0f

    var xmag: Float = 0f
    var ymag: Float = 0f

    override val defaultName: String
        get() = "Camera"

    override fun readJson() {
        super.readJson()

        type = json.string("type")

        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-perspective
        json.obj("perspective") {
            json.float("aspectRatio") { aspectRatio = it }
            if (json.contains("yfov")) {
                yfov = json.float("yfov")
            } else {
                LOG.error("GLTFCamera: parsing camera $name: yfov must be set")
            }

            json.float("zfar") {
                zfar = it
                camera.far = it
            }
            json.float("znear") {
                znear = it
                camera.near = it
            }
        }

        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-orthographic
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

        ready()
    }

    override fun writeJson() {
        super.writeJson()

        json["type"] = type
        when (type) {
            "perspective" -> {
                json.setObj("perspective") {
                    if (aspectRatio != 0f) set("aspectRatio", aspectRatio)
                    set("yfov", yfov)
                    if (zfar != 0f) set("zfar", zfar)
                    set("znear", znear)
                }
            }
            "orthographic" -> {
                json.setObj("orthographic") {
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