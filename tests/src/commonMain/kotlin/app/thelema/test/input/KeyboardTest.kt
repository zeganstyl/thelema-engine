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

package app.thelema.test.input

import app.thelema.input.IKeyListener
import app.thelema.input.KB
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class KeyboardTest: Test {
    override val name: String
        get() = "Keyboard"

    override fun testMain() {
        LOG.info("Press some key on keyboard")

        KB.addListener(object: IKeyListener {
            override fun keyDown(keycode: Int) {
                LOG.info("keyDown: $keycode (${KB.toString(keycode)})")
            }

            override fun keyUp(keycode: Int) {
                LOG.info("keyUp: $keycode (${KB.toString(keycode)})")
            }

            override fun keyTyped(character: Char) {
                LOG.info("keyTyped: $character")
            }
        })
    }
}
