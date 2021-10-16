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

package app.thelema.net

interface IHttp {
    val originUrl: String
        get() = "http://localhost"

    fun get(
        url: String,
        respondAsBytes: Boolean = false,
        headers: Map<String, String>? = null,
        error: (status: Int) -> Unit = {},
        ready: (response: IHttpResponse) -> Unit
    )

    fun head(url: String, headers: Map<String, String>? = null): Int

    fun postText(
        url: String,
        body: String,
        respondAsBytes: Boolean = false,
        headers: Map<String, String>? = null,
        error: (status: Int) -> Unit = {},
        ready: (response: IHttpResponse) -> Unit
    )

    fun postFormData(
        url: String,
        body: IFormData.() -> Unit,
        respondAsBytes: Boolean = false,
        headers: Map<String, String>? = null,
        error: (status: Int) -> Unit = {},
        ready: (response: IHttpResponse) -> Unit
    )
}

lateinit var HTTP: IHttp

fun httpIsSuccess(response: Int): Boolean = response in 200..299