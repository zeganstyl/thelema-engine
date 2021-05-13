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

package app.thelema.res

import app.thelema.font.BitmapFont
import app.thelema.fs.IFile
import app.thelema.gltf.GLTF

class DefaultResourceProvider: IResourceProvider {
    override val name: String
        get() = "Default"

    val extensions = HashMap<String, String>()

    init {
        extensions["gltf"] = GLTF::class.simpleName!!
        extensions["glb"] = GLTF::class.simpleName!!
        extensions["vrm"] = GLTF::class.simpleName!!
        extensions["fnt"] = BitmapFont::class.simpleName!!
    }

    override fun canLoad(file: IFile): Boolean =
        extensions.containsKey(file.path.substringAfterLast('.').toLowerCase())

    override fun provideLoader(file: IFile): String =
        extensions[file.path.substringAfterLast('.').toLowerCase()]!!

}
