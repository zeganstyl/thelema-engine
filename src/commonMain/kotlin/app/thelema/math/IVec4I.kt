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

package app.thelema.math

/** 4-dimension vector of integers */
interface IVec4I {
    var x: Int
    var y: Int
    var z: Int
    var w: Int

    var r: Int
        get() = x
        set(value) { x = value }
    var g: Int
        get() = y
        set(value) { y = value }
    var b: Int
        get() = z
        set(value) { z = value }
    var a: Int
        get() = w
        set(value) { w = value }
}