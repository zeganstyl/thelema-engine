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

package org.ksdfv.thelema.utils

import org.ksdfv.thelema.net.NET

/** To check loading progress of models, sounds, or may be even procedural generating resources
 * @author zeganstyl*/
interface IResourceLoader {
    var name: String

    /** To check when asynchronous loading */
    var isLoading: Boolean

    var isLoaded: Boolean

    var loadedElements: Int

    var maxNumLoadingElements: Int

    val loadingProgress: Float
        get() = loadedElements.toFloat() / maxNumLoadingElements.toFloat()

    /** What is loading or generating. When loading completed, object must be linked here */
    var data: Any?

    /** Resource must be not released until all requesters unlock this resource */
    val requesters: List<Any>

    fun lockResource(requester: Any)
    fun unlockResource(requester: Any)

    /** Loading in this method must be not depending on OpenGL thread.
     * If need calls on OpenGL thread, use GL.call.
     * For response status meaning use [NET] */
    fun load(response: (status: Int) -> Unit)

    /** [data] must be set to null */
    fun destroy()
}