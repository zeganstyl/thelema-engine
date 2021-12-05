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

package app.thelema.test.android

import android.app.Activity
import android.os.Bundle
import app.thelema.android.AndroidApp
import app.thelema.test.MainTest

class AndroidMain : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            AndroidApp(this) { MainTest() }.view.also {
                val metrics = resources.displayMetrics

                val scale = 200
                it.holder.setFixedSize((metrics.widthPixels * scale / metrics.xdpi).toInt(), (metrics.heightPixels * scale / metrics.ydpi).toInt())
            }
        )
    }
}