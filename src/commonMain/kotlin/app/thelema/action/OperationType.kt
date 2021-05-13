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

package app.thelema.action

object OperationType {
    const val Plus = "+"
    const val Minus = "-"
    const val Multiply = "*"
    const val Divide = "/"
    const val Assign = "="
    const val PlusAssign = "+="
    const val MinusAssign = "-="
    const val MultiplyAssign = "*="
    const val DivideAssign = "/="

    const val Not = "!"
    const val Equal = "=="
    const val NotEqual = "!="
    const val And = "&&"
    const val Or = "||"
    const val Greater = ">"
    const val GreaterOrEqual = ">="
    const val Less = "<"
    const val LessOrEqual = "<="
}