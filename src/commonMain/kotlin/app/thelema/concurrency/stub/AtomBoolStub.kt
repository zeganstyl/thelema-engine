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

package app.thelema.concurrency.stub

import app.thelema.concurrency.AtomBool

class AtomBoolStub(override var value: Boolean): AtomBool {
    override fun lazySet(newValue: Boolean) {
        this.value = newValue
    }

    override fun compareAndSet(expect: Boolean, newValue: Boolean): Boolean {
        if (expect == value) {
            value = newValue
            return true
        }
        return false
    }

    override fun getAndSet(newValue: Boolean): Boolean {
        val result = newValue
        this.value = newValue
        return result
    }
}