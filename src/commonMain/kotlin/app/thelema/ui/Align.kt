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

package app.thelema.ui

/** Provides bit flag constants for alignment.
 * @author Nathan Sweet
 */
object Align {
    const val center = 1 shl 0
    const val top = 1 shl 1
    const val bottom = 1 shl 2
    const val left = 1 shl 3
    const val right = 1 shl 4
    const val topLeft = top or left
    const val topRight = top or right
    const val bottomLeft = bottom or left
    const val bottomRight = bottom or right
}
