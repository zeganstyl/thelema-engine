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

package app.thelema.studio.widget

import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.ui.*

class IntField(value: Int = 0): TextField(), PropertyProvider<Int> {
    var value: Int = value
        set(value) {
            if (field != value) {
                field = value
                text = value.toString()
            }
        }

    override var set: (value: Int) -> Unit = {}
    override var get: () -> Int = { 0 }

    init {
        text = value.toString()

        addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> {
                        val v = text.toIntOrNull()
                        if (v != null) set(v)
                    }
                    KEY.ESCAPE -> {
                        text = value.toString()
                    }
                    KEY.Z -> {
                        if (KEY.ctrlPressed) text = value.toString()
                    }
                }
                return super.keyDown(event, keycode)
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        if (!focused) {
            value = get()
        }
    }

    fun onChangedInt(call: (newValue: Int) -> Unit): ChangeListener {
        val listener = object : ChangeListener {
            override fun changed(event: Event, actor: Actor) {
                call(value)
            }
        }
        addListener(listener)
        return listener
    }
}
