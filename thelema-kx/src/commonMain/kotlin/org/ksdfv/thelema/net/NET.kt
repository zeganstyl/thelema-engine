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

package org.ksdfv.thelema.net

/** Network */
object NET {
    const val OK = 200
    const val Forbidden = 403
    const val NotFound = 404
    const val BadGateway = 502

    fun isInfo(response: Int): Boolean = response in 100..199
    fun isSuccess(response: Int): Boolean = response in 200..299
    fun isRedirection(response: Int): Boolean = response in 300..399
    fun isClientError(response: Int): Boolean = response in 400..499
    fun isServerError(response: Int): Boolean = response in 500..599
}
