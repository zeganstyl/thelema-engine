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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.app.IApp
import kotlin.math.abs

/** @author zeganstyl */
interface Test {
    val name: String

    /** Here must be code that will be executed after application created [IApp] */
    fun testMain()

    fun check(name: String, isOK: Boolean, mode: Int = PRINT_ALL): Boolean {
        if (mode != NO_PRINT) {
            if (isOK) {
                if (mode == PRINT_ALL) println("$name: OK")
                return true
            } else {
                println("$name: FAILED")
                return false
            }
        }
        return false
    }

    fun check(name: String, vararg isOK: Boolean, mode: Int = PRINT_ALL): Boolean {
        if (mode != NO_PRINT) {
            if (isOK.contains(false)) {
                println("$name: FAILED")
                return false
            } else {
                if (mode == PRINT_ALL) println("$name: OK")
                return true
            }
        }
        return false
    }

    fun isEqual(x: Float, y: Float, tolerance: Float = 0.0001f): Boolean =
        abs(x - y) <= tolerance

    companion object {
        const val NO_PRINT = 0
        const val PRINT_FAILED = 1
        const val PRINT_ALL = 2
    }
}
