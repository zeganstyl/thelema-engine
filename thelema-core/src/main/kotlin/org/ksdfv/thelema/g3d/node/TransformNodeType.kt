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

package org.ksdfv.thelema.g3d.node

/** Node type will define, what transform data may be stored in node.
 *
 * @author zeganstyl */
object TransformNodeType {
    const val None = 0

    /** Translation, rotation, scale, also known as TRS */
    const val TRS = 1

    /** Only 3d position data. */
    const val Translation = 2

    /** 3d position data and 3d scaling. */
    const val TranslationScale = 3

    /** 3d position data and quaternion (vec4). */
    const val TranslationRotation = 4

    /** 3d position data and angle around Y-axis */
    const val TranslationRotationY = 5

    /** 3d position data and single scaling value. */
    const val TranslationScaleProportional = 6
}