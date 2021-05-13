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

package app.thelema.gltf.tetf

import app.thelema.app.APP
import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.gltf.*
import app.thelema.json.IJsonObject
import app.thelema.utils.LOG

/** @author zeganstyl */
class TETFLibrary(array: IGLTFArray): GLTFArrayElementAdapter(array) {
    var uri: String = ""
    var fileLocation: Int = FileLocation.Classpath
    var scene: GLTFScene? = null

    var tf = TETF()

    override fun readJson() {
        super.readJson()

        uri = json.string("uri")

        fileLocation = when (json.string("fileLocation")) {
            "classpath" -> FileLocation.Classpath
            "absolute" -> FileLocation.Absolute
            "external" -> FileLocation.External
            "internal" -> FileLocation.Internal
            "local" -> FileLocation.Local
            else -> throw IllegalStateException("Unknown file location")
        }

//        val data = tf
//        val file = FS.file(uri, fileLocation)
//        data.source = file
//        data.directory = file.parent()
//        data.conf = gltf.conf
//
//        val sceneIndex = gltf.getArray("libraries").size
//        if (data.loadOnSeparateThread) {
//            APP.thread {
//                load(sceneIndex, json)
//            }
//        } else {
//            load(sceneIndex, json)
//        }
    }

    private fun load(sceneIndex: Int, json: IJsonObject) {
//        tf.onLoaded(
//            ready = {
//                scene = tf.scenes.getOrNull(json.int("scene")) as GLTFScene?
//
//                val scene = scene
//                if (scene != null) {
//                    (gltf.scenes.getOrNull(json.int("attachToScene", -1)) as GLTFScene?)?.scene?.scenes?.add(scene.scene)
//                }
//            },
//            error = {
//                LOG.info("can't read $uri, status: $it")
//            }
//        )

        ready()
    }

    override fun writeJson() {
        super.writeJson()

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
