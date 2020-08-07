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

import org.ksdfv.thelema.input.IMouseListener
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class MouseTest: Test {
    override val name: String
        get() = "Mouse"

    override fun testMain() {
        LOG.info("Press some button on mouse or move mouse over window")

        MOUSE.addListener(object: IMouseListener {
            override fun buttonDown(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                LOG.info("buttonDown: $button")
            }

            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                LOG.info("buttonUp: $button")
            }

            override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
                LOG.info("dragged: $screenX, $screenY")
            }

            override fun moved(screenX: Int, screenY: Int) {
                LOG.info("moved: $screenX, $screenY")
            }

            override fun scrolled(amount: Int) {
                LOG.info("scrolled: $amount")
            }
        })
    }
}
