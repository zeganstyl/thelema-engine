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

internal class StringTokenizer constructor(
    str: String,
    delim: String = " \t\n\r",
    returnDelims: Boolean = false
) {
    private var currentPosition = 0
    private var newPosition: Int
    private val maxPosition: Int
    private val str: String
    private var delimiters: String = ""
    private val retDelims: Boolean
    private var delimsChanged: Boolean

    /**
     * maxDelimCodePoint stores the value of the delimiter character with the
     * highest value. It is used to optimize the detection of delimiter
     * characters.
     *
     * It is unlikely to provide any optimization benefit in the
     * hasSurrogates case because most string characters will be
     * smaller than the limit, but we keep it so that the two code
     * paths remain similar.
     */
    private var maxDelimCodePoint = 0

    /**
     * If delimiters include any surrogates (including surrogate
     * pairs), hasSurrogates is true and the tokenizer uses the
     * different code path. This is because String.indexOf(int)
     * doesn't handle unpaired surrogates as a single character.
     */
    private var hasSurrogates = false

    /**
     * When hasSurrogates is true, delimiters are converted to code
     * points and isDelimiter(int) is used to determine if the given
     * codepoint is a delimiter.
     */
    private var delimiterCodePoints: IntArray = IntArray(0)

    /**
     * Set maxDelimCodePoint to the highest char in the delimiter set.
     */
    private fun setMaxDelimCodePoint() {
        if (delimiters.isEmpty()) {
            maxDelimCodePoint = 0
            return
        }
        var m = 0
        var c: Int
        var count = 0
        var i = 0
        while (i < delimiters.length) {
            c = delimiters[i].toInt()
            if (c >= '\uD800'.toInt() && c <= '\uDFFF'.toInt()) {
                c = delimiters[i].toInt() and 0xFF
                hasSurrogates = true
            }
            if (m < c) m = c
            count++
            i += 1
        }
        maxDelimCodePoint = m
        if (hasSurrogates) {
            delimiterCodePoints = IntArray(count)
            var i = 0
            var j = 0
            while (i < count) {
                c = delimiters[j].toInt() and 0xFF
                delimiterCodePoints[i] = c
                i++
                j += 1
            }
        }
    }

    /**
     * Skips delimiters starting from the specified position. If retDelims
     * is false, returns the index of the first non-delimiter character at or
     * after startPos. If retDelims is true, startPos is returned.
     */
    private fun skipDelimiters(startPos: Int): Int {
        var position = startPos
        while (!retDelims && position < maxPosition) {
            if (!hasSurrogates) {
                val c = str[position]
                if (c.toInt() > maxDelimCodePoint || delimiters.indexOf(c) < 0) break
                position++
            } else {
                val c: Int = str[position].toInt() and 0xFF
                if (c > maxDelimCodePoint || !isDelimiter(c)) {
                    break
                }
                position += 1
            }
        }
        return position
    }

    /**
     * Skips ahead from startPos and returns the index of the next delimiter
     * character encountered, or maxPosition if no such delimiter is found.
     */
    private fun scanToken(startPos: Int): Int {
        var position = startPos
        while (position < maxPosition) {
            if (!hasSurrogates) {
                val c = str[position]
                if (c.toInt() <= maxDelimCodePoint && delimiters.indexOf(c) >= 0) break
                position++
            } else {
                val c: Int = str[position].toInt() and 0xFF
                if (c <= maxDelimCodePoint && isDelimiter(c)) break
                position += 1
            }
        }
        if (retDelims && startPos == position) {
            if (!hasSurrogates) {
                val c = str[position]
                if (c.toInt() <= maxDelimCodePoint && delimiters.indexOf(c) >= 0) position++
            } else {
                val c: Int = str[position].toInt() and 0xFF
                if (c <= maxDelimCodePoint && isDelimiter(c)) position += 1
            }
        }
        return position
    }

    private fun isDelimiter(codePoint: Int): Boolean {
        for (delimiterCodePoint in delimiterCodePoints) {
            if (delimiterCodePoint == codePoint) {
                return true
            }
        }
        return false
    }

    /**
     * Tests if there are more tokens available from this tokenizer's string.
     * If this method returns `true`, then a subsequent call to
     * `nextToken` with no argument will successfully return a token.
     *
     * @return  `true` if and only if there is at least one token
     * in the string after the current position; `false`
     * otherwise.
     */
    fun hasMoreTokens(): Boolean {
        /*
         * Temporarily store this position and use it in the following
         * nextToken() method only if the delimiters haven't been changed in
         * that nextToken() invocation.
         */
        newPosition = skipDelimiters(currentPosition)
        return newPosition < maxPosition
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return     the next token from this string tokenizer.
     * @exception  NoSuchElementException  if there are no more tokens in this
     * tokenizer's string.
     */
    fun nextToken(): String {
        /*
         * If next position already computed in hasMoreElements() and
         * delimiters have changed between the computation and this invocation,
         * then use the computed value.
         */
        currentPosition = if (newPosition >= 0 && !delimsChanged) newPosition else skipDelimiters(currentPosition)

        /* Reset these anyway */delimsChanged = false
        newPosition = -1
        if (currentPosition >= maxPosition) throw NoSuchElementException()
        val start = currentPosition
        currentPosition = scanToken(currentPosition)
        return str.substring(start, currentPosition)
    }

    /**
     * Returns the next token in this string tokenizer's string. First,
     * the set of characters considered to be delimiters by this
     * `StringTokenizer` object is changed to be the characters in
     * the string `delim`. Then the next token in the string
     * after the current position is returned. The current position is
     * advanced beyond the recognized token.  The new delimiter set
     * remains the default after this call.
     *
     * @param      delim   the new delimiters.
     * @return     the next token, after switching to the new delimiter set.
     * @exception  NoSuchElementException  if there are no more tokens in this
     * tokenizer's string.
     * @exception NullPointerException if delim is `null`
     */
    fun nextToken(delim: String): String {
        delimiters = delim

        /* delimiter string specified, so set the appropriate flag. */delimsChanged = true
        setMaxDelimCodePoint()
        return nextToken()
    }

    /**
     * Returns the same value as the `hasMoreTokens`
     * method. It exists so that this class can implement the
     * `Enumeration` interface.
     *
     * @return  `true` if there are more tokens;
     * `false` otherwise.
     * @see java.util.Enumeration
     *
     * @see java.util.StringTokenizer.hasMoreTokens
     */
    fun hasMoreElements(): Boolean {
        return hasMoreTokens()
    }

    /**
     * Returns the same value as the `nextToken` method,
     * except that its declared return value is `Object` rather than
     * `String`. It exists so that this class can implement the
     * `Enumeration` interface.
     *
     * @return     the next token in the string.
     * @exception  NoSuchElementException  if there are no more tokens in this
     * tokenizer's string.
     * @see java.util.Enumeration
     *
     * @see java.util.StringTokenizer.nextToken
     */
    fun nextElement(): Any {
        return nextToken()
    }

    /**
     * Calculates the number of times that this tokenizer's
     * `nextToken` method can be called before it generates an
     * exception. The current position is not advanced.
     *
     * @return  the number of tokens remaining in the string using the current
     * delimiter set.
     * @see java.util.StringTokenizer.nextToken
     */
    fun countTokens(): Int {
        var count = 0
        var currpos = currentPosition
        while (currpos < maxPosition) {
            currpos = skipDelimiters(currpos)
            if (currpos >= maxPosition) break
            currpos = scanToken(currpos)
            count++
        }
        return count
    }
    /**
     * Constructs a string tokenizer for the specified string. All
     * characters in the `delim` argument are the delimiters
     * for separating tokens.
     *
     *
     * If the `returnDelims` flag is `true`, then
     * the delimiter characters are also returned as tokens. Each
     * delimiter is returned as a string of length one. If the flag is
     * `false`, the delimiter characters are skipped and only
     * serve as separators between tokens.
     *
     *
     * Note that if `delim` is `null`, this constructor does
     * not throw an exception. However, trying to invoke other methods on the
     * resulting `StringTokenizer` may result in a
     * `NullPointerException`.
     *
     * @param   str            a string to be parsed.
     * @param   delim          the delimiters.
     * @param   returnDelims   flag indicating whether to return the delimiters
     * as tokens.
     * @exception NullPointerException if str is `null`
     */
    /**
     * Constructs a string tokenizer for the specified string. The
     * tokenizer uses the default delimiter set, which is
     * `"&nbsp;&#92;t&#92;n&#92;r&#92;f"`: the space character,
     * the tab character, the newline character, the carriage-return character,
     * and the form-feed character. Delimiter characters themselves will
     * not be treated as tokens.
     *
     * @param   str   a string to be parsed.
     * @exception NullPointerException if str is `null`
     */
    /**
     * Constructs a string tokenizer for the specified string. The
     * characters in the `delim` argument are the delimiters
     * for separating tokens. Delimiter characters themselves will not
     * be treated as tokens.
     *
     *
     * Note that if `delim` is `null`, this constructor does
     * not throw an exception. However, trying to invoke other methods on the
     * resulting `StringTokenizer` may result in a
     * `NullPointerException`.
     *
     * @param   str     a string to be parsed.
     * @param   delim   the delimiters.
     * @exception NullPointerException if str is `null`
     */
    init {
        newPosition = -1
        delimsChanged = false
        this.str = str
        maxPosition = str.length
        delimiters = delim
        retDelims = returnDelims
        setMaxDelimCodePoint()
    }
}