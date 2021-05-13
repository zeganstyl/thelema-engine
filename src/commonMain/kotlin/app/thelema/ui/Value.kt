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

package app.thelema.ui


/** Value placeholder, allowing the value to be computed on request. Values are provided an actor for context which reduces the
 * number of value instances that need to be created and reduces verbosity in code that specifies values.
 * @author Nathan Sweet
 */
abstract class Value {
    /** Calls [get] with null.  */
    fun get(): Float {
        return get(null)
    }

    /** @param context May be null.
     */
    abstract operator fun get(context: Actor?): Float

    /** A fixed value that is not computed each time it is used.
     * @author Nathan Sweet
     */
    class Fixed(private val value: Float) : Value() {
        override fun get(context: Actor?): Float {
            return value
        }

        override fun toString(): String {
            return value.toString()
        }

        companion object {
            val cache = arrayOfNulls<Fixed>(111)
            fun valueOf(value: Float): Fixed {
                if (value == 0f) return zero
                if (value >= -10 && value <= 100 && (value == value.toInt().toFloat())) {
                    var fixed = cache[value.toInt() + 10]
                    if (fixed == null) {
                        fixed = Fixed(value)
                        cache[value.toInt() + 10] = fixed
                    }
                    return fixed
                }
                return Fixed(value)
            }
        }

    }

    companion object {
        /** A value that is always zero.  */
        val zero = Fixed(0f)
        /** Value that is the minWidth of the actor in the cell.  */
        var minWidth: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).minWidth else context?.width ?: 0f
            }
        }
        /** Value that is the minHeight of the actor in the cell.  */
        var minHeight: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).minHeight else context?.height ?: 0f
            }
        }
        /** Value that is the prefWidth of the actor in the cell.  */
        var prefWidth: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).prefWidth else context?.width ?: 0f
            }
        }
        /** Value that is the prefHeight of the actor in the cell.  */
        var prefHeight: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).prefHeight else context?.height ?: 0f
            }
        }
        /** Value that is the maxWidth of the actor in the cell.  */
        var maxWidth: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).maxWidth else context?.width ?: 0f
            }
        }
        /** Value that is the maxHeight of the actor in the cell.  */
        var maxHeight: Value = object : Value() {
            override fun get(context: Actor?): Float {
                return if (context is Layout) (context as Layout).maxHeight else context?.height ?: 0f
            }
        }

        /** Returns a value that is a percentage of the actor's width.  */
        fun percentWidth(percent: Float): Value {
            return object : Value() {
                override operator fun get(context: Actor?): Float {
                    return context!!.width * percent
                }
            }
        }

        /** Returns a value that is a percentage of the actor's height.  */
        fun percentHeight(percent: Float): Value {
            return object : Value() {
                override operator fun get(context: Actor?): Float {
                    return context!!.height * percent
                }
            }
        }

        /** Returns a value that is a percentage of the specified actor's width. The context actor is ignored.  */
        fun percentWidth(percent: Float, actor: Actor?): Value {
            requireNotNull(actor) { "actor cannot be null." }
            return object : Value() {
                override fun get(context: Actor?): Float {
                    return actor.width * percent
                }
            }
        }

        /** Returns a value that is a percentage of the specified actor's height. The context actor is ignored.  */
        fun percentHeight(percent: Float, actor: Actor?): Value {
            requireNotNull(actor) { "actor cannot be null." }
            return object : Value() {
                override fun get(context: Actor?): Float {
                    return actor.height * percent
                }
            }
        }
    }
}
