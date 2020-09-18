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

import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.g3d.gltf.*
import org.ksdfv.thelema.json.IJsonObject

/** @author zeganstyl */
class THTF(
    source: Any,
    sourceType: GLTFSourceType,
    directory: IFile
): GLTF(source, sourceType, directory), ITHTF {
    constructor(
        file: IFile,
        sourceType: GLTFSourceType = GLTFSourceType.GLTFFile,
        directory: IFile = file.parent()
    ): this(file as Any, sourceType, directory)

    override val shaders: IGLTFArray<THTFShader> = GLTFArray("shaders")
    override val libraries: IGLTFArray<THTFLibrary> = GLTFArray("libraries")

    override fun createMaterial(): IGLTFMaterial =
        THTFMaterial(thtf = this, elementIndex = materials.size)

    override fun read(json: IJsonObject) {
        super.read(json)

        json.obj("extra") {
            array("shaders") {
                objs {
                    val shader = THTFShader(thtf = this@THTF, elementIndex = shaders.size)
                    shaders.add(shader)
                    shader.read(this)
                }
            }

            array("libraries") {
                objs {
                    val library = THTFLibrary(thtf = this@THTF, elementIndex = libraries.size)
                    libraries.add(library)
                    library.read(this)
                }
            }
        }
    }

    override fun write(json: IJsonObject) {
        super.write(json)

        json.set("extra") {
            if (shaders.isNotEmpty()) {
                setArray("shaders") {
                    for (i in shaders.indices) {
                        add(shaders[i])
                    }
                }
            }

            if (libraries.isNotEmpty()) {
                setArray("libraries") {
                    for (i in libraries.indices) {
                        add(libraries[i])
                    }
                }
            }
        }
    }
}
