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

/** Stores glyphs and positions for a piece of text which is a single color and does not span multiple lines.
 * @author Nathan Sweet
 */
class GlyphRun {
    var glyphs = ArrayList<Glyph>()
    /** Contains glyphs.size+1 entries: First entry is X offset relative to the drawing position. Subsequent entries are the X
     * advance relative to previous glyph position. Last entry is the width of the last glyph.  */
    var xAdvances = ArrayList<Float>()
    var x = 0f
    var y = 0f
    var width = 0f
    var color = -1
    fun reset() {
        glyphs.clear()
        xAdvances.clear()
        width = 0f
    }

    override fun toString(): String {
        val buffer = StringBuilder(glyphs.size)
        val glyphs = glyphs
        var i = 0
        val n = glyphs.size
        while (i < n) {
            val g = glyphs[i]
            buffer.append(g.id.toChar())
            i++
        }
        buffer.append(", #")
        buffer.append(color)
        buffer.append(", ")
        buffer.append(x)
        buffer.append(", ")
        buffer.append(y)
        buffer.append(", ")
        buffer.append(width)
        return buffer.toString()
    }
}

fun <T> MutableList<T>.addAll(array: List<T>, start: Int, count: Int) {
    require(start + count <= array.size) { "start + count must be <= size: " + start + " + " + count + " <= " + array.size }
    for (i in count downTo 0) {
        add(start, array[i])
    }
}

fun <T> MutableList<T>.removeRange(fromIndex: Int, toIndex: Int) {
    var i = toIndex
    while (i >= fromIndex) {
        removeAt(i)
        i--
    }
}

fun <T> MutableList<T>.truncate(newSize: Int) {
    if (newSize < size) {
        removeRange(newSize, size-1)
    }
}