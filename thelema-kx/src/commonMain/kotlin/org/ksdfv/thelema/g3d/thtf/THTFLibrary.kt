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

package org.ksdfv.thelema.g3d.thtf

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.g3d.gltf.GLTFScene
import org.ksdfv.thelema.g3d.gltf.GLTFSourceType
import org.ksdfv.thelema.g3d.gltf.IGLTF
import org.ksdfv.thelema.g3d.gltf.IGLTFArrayElement
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class THTFLibrary(
    val thtf: ITHTF,
    override var elementIndex: Int,
    override var name: String = ""
): IJsonObjectIO, IGLTFArrayElement {
    override val gltf: IGLTF
        get() = thtf

    var uri: String = ""
    var fileLocation: Int = FileLocation.Classpath
    var scene: GLTFScene? = null

    var tf = THTF(FS.internal(""), GLTFSourceType.GLTFFile)

    override fun read(json: IJsonObject) {
        uri = json.string("uri")

        fileLocation = when (json.string("fileLocation")) {
            "classpath" -> FileLocation.Classpath
            "absolute" -> FileLocation.Absolute
            "external" -> FileLocation.External
            "internal" -> FileLocation.Internal
            "local" -> FileLocation.Local
            else -> throw IllegalStateException("Unknown file location")
        }

        val data = tf
        val file = FS.file(uri, fileLocation)
        data.source = file
        data.directory = file.parent()
        data.conf = gltf.conf

        val sceneIndex = thtf.libraries.size
        if (data.conf.separateThread) {
            APP.parallelJob {
                load(sceneIndex, json)
            }
        } else {
            load(sceneIndex, json)
        }
    }

    private fun load(sceneIndex: Int, json: IJsonObject) {
        tf.load { status ->
            if (NET.isSuccess(status)) {
                scene = tf.scenes.getOrNull(json.int("scene"))

                val scene = scene
                if (scene != null) {
                    gltf.scenes.getOrNull(json.int("attachToScene", -1))?.scene?.scenes?.add(scene.scene)
                }
            } else {
                LOG.info("can't read $uri, status: $status")
            }
        }

        thtf.libraries.ready(sceneIndex)
    }

    override fun write(json: IJsonObject) {
        json["uri"] = uri
        json["fileLocation"] = when (fileLocation) {
            FileLocation.Classpath -> "classpath"
            FileLocation.Absolute -> "absolute"
            FileLocation.External -> "external"
            FileLocation.Internal -> "internal"
            FileLocation.Local -> "local"
            else -> throw IllegalStateException("Unknown file location")
        }
    }

    override fun destroy() {}
}
