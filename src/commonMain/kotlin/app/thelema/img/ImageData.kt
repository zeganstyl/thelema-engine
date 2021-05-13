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

package app.thelema.img

import app.thelema.data.IByteData
import app.thelema.gl.GL_RGBA
import app.thelema.gl.GL_UNSIGNED_BYTE

/** @author zeganstyl */
class ImageData(
    override var width: Int,
    override var height: Int,
    override var bytes: IByteData,
    override var glInternalFormat: Int = GL_RGBA,
    override var glPixelFormat: Int = GL_RGBA,
    override var glType: Int = GL_UNSIGNED_BYTE,
    override var uri: String = "",
    override var name: String = uri
): IImageData {
    override fun destroy() {}
}
