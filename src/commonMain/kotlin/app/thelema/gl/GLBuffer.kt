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

package app.thelema.gl

import app.thelema.data.IByteData

class GLBuffer(override var bytes: IByteData, override var target: Int = GL_ARRAY_BUFFER): IGLBuffer {
    override var bufferHandle: Int = 0

    override var isBufferLoadingRequested: Boolean = true

    override var usage: Int = GL_STATIC_DRAW
}
