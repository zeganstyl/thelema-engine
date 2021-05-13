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

package app.thelema.test

/** @author zeganstyl */
open class TestGroup(val name: String = "", val array: MutableList<Test> = ArrayList()): MutableList<Test> by array {
    constructor(name: String, vararg tests: Test): this(name) {
        addAll(tests)
    }

    val groups = ArrayList<TestGroup>()

    fun group(name: String, block: TestGroup.() -> Unit) {
        val group = TestGroup(name)
        block(group)
        groups.add(group)
    }

    fun group(name: String, vararg tests: Test) {
        val group = TestGroup(name, *tests)
        groups.add(group)
    }
}