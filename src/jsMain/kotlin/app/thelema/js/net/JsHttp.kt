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

package app.thelema.js.net

import app.thelema.net.HTTP
import app.thelema.net.IFormData
import app.thelema.net.IHttp
import app.thelema.net.IHttpResponse
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

class JsHttp: IHttp {
    override fun get(
        url: String,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        request(url, "GET", respondAsBytes, headers, error, ready).send()
    }

    override fun head(url: String, headers: Map<String, String>?): Int {
        val http = XMLHttpRequest()
        http.open("HEAD", url, false)
        http.send()
        return http.status.toInt()
    }

    override fun postText(
        url: String,
        body: String,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        request(url, "POST", respondAsBytes, headers, error, ready).send(body)
    }

    private fun request(
        url: String,
        method: String,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ): XMLHttpRequest {
        val xhr = XMLHttpRequest()
        headers?.entries?.forEach { xhr.setRequestHeader(it.key, it.value) }
        if (respondAsBytes) xhr.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
        xhr.open(method, url, true)
        xhr.onload = {
            if (xhr.readyState.toInt() == 4) {
                if (HTTP.isSuccess(xhr.status.toInt())) {
                    ready(TvmHttpResponse(xhr))
                } else {
                    error(xhr.status.toInt())
                }
            }
        }
        return xhr
    }

    override fun postFormData(
        url: String,
        body: IFormData.() -> Unit,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        request(url, "POST", respondAsBytes, headers, error, ready).send(JsFormData().apply(body).form)
    }
}