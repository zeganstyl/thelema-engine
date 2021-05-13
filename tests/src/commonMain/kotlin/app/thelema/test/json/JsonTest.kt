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

package app.thelema.test.json

import app.thelema.app.APP
import app.thelema.json.JSON
import app.thelema.test.Test
import app.thelema.utils.LOG

class JsonTest: Test {
    override val name: String
        get() = "JSON"

    override fun testMain() {
        val floatValue = 123f
        val intValue = 123
        val boolValue = false
        val stringValue = "qwerty"

        val jsonString = JSON.printObject {
            set("floatValue", floatValue)
            set("intValue", intValue)
            set("boolValue", boolValue)
            set("stringValue", stringValue)
            setObj("nestedObject") {
                setArray("array") {
                    add(floatValue)
                }
            }
        }

        LOG.info("SERIALIZATION:")
        LOG.info(jsonString)

        val parsed = JSON.parseObject(jsonString)


        parsed.obj("nestedObject")

        val parsedFloat = parsed.float("floatValue")
        val parsedInt = parsed.int("intValue")
        val parsedBool = parsed.bool("boolValue")
        val parsedString = parsed.string("stringValue")
        val nestedObjectArrayValue = parsed.obj("nestedObject").array("array").float(0)

        val parsingMessage = """
-----
PARSING:
floatValue = $parsedFloat - ${if (parsedFloat != floatValue) "not " else ""}correct
intValue = $parsedInt - ${if (parsedInt != intValue) "not " else ""}correct
boolValue = $parsedBool - ${if (parsedBool != boolValue) "not " else ""}correct
stringValue = $parsedString - ${if (parsedString != stringValue) "not " else ""}correct
nested object array value: $nestedObjectArrayValue - ${if (nestedObjectArrayValue != floatValue) "not " else ""}correct
"""

        LOG.info(parsingMessage)

        APP.messageBox("JSON", "SERIALIZATION:\n$jsonString\n$parsingMessage")
    }
}