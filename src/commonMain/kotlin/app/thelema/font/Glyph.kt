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

package app.thelema.font

/** Represents a single character in a font page.  */
class Glyph {
    var id = 0
    var srcX = 0
    var srcY = 0
    var width = 0
    var height = 0
    var u = 0f
    var v = 0f
    var u2 = 0f
    var v2 = 0f
    var xoffset = 0
    var yoffset = 0
    var xadvance = 0
    var kerning: Array<ByteArray?>? = null
    var fixedWidth = false
    /** The index to the texture page that holds this glyph.  */
    var page = 0

    fun getKerning(ch: Char): Int {
        if (kerning != null) {
            val page = kerning!![ch.toInt() ushr BitmapFont.LOG2_PAGE_SIZE]
            if (page != null) return page[ch.toInt() and BitmapFont.PAGE_SIZE - 1].toInt()
        }
        return 0
    }

    fun setKerning(ch: Int, value: Int) {
        if (kerning == null) kerning = arrayOfNulls(BitmapFont.PAGES)
        var page = kerning!![ch ushr BitmapFont.LOG2_PAGE_SIZE]
        if (page == null) {
            page = ByteArray(BitmapFont.PAGE_SIZE)
            kerning!![ch ushr BitmapFont.LOG2_PAGE_SIZE] = page
        }
        page[ch and BitmapFont.PAGE_SIZE - 1] = value.toByte()
    }

    override fun toString(): String {
        return id.toChar().toString()
    }
}