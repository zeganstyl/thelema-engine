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

package app.thelema.test.data

import app.thelema.data.DATA
import app.thelema.test.Test

class DataTest: Test {
    override val name: String
        get() = "Data"

    override fun testMain() {
        val bytes = DATA.bytes(32)

        val floats = bytes.floatView()

        floats[0] = 123f
        floats[1] = 0.123f
        checkMustEqual("IByteData.getFloat()", bytes.getFloat(0), 123f)
        checkMustEqual("IByteData.getFloat()", bytes.getFloat(4), 0.123f)

        bytes.putFloat(8, 345f)
        bytes.putFloat(12, 0.345f)
        checkMustEqual("IByteData.putFloat()", floats[2], 345f)
        checkMustEqual("IByteData.putFloat()", floats[3], 0.345f)


        val ints = bytes.intView()

        ints[0] = 123
        ints[1] = -234
        checkMustEqual("IByteData.getInt()", bytes.getInt(0), 123)
        checkMustEqual("IByteData.getInt()", bytes.getInt(4), -234)

        bytes.putInt(8, 345)
        bytes.putInt(12, -234)
        checkMustEqual("IByteData.putInt()", ints[2], 345)
        checkMustEqual("IByteData.putInt()", ints[3], -234)

        val strBytes = DATA.bytes("qwertyQWERTY123234")

        checkMustEqual("IByteData.toStringUTF8()", strBytes.toStringUTF8(), "qwertyQWERTY123234")
        checkMustEqual("IByteData.toStringUTF8()", strBytes.byteView(6, 6).toStringUTF8(), "QWERTY")
    }
}
