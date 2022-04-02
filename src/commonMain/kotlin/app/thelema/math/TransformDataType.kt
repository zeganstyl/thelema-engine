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

package app.thelema.math

/** Node type will define, what transform data may be stored in node.
 *
 * @author zeganstyl */
object TransformDataType {
    const val None = "None"

    /** Translation, rotation, scale, also known as TRS */
    const val TRS = "TRS"

    /** Only 3d position data. */
    const val Translation = "Translation"

    /** 3d position data and 3d scaling. */
    const val TranslationScale = "TranslationScale"

    /** 3d position data and quaternion (vec4). */
    const val TranslationRotation = "TranslationRotation"
}
