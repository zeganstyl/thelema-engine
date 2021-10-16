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

import org.recast4j.recast.AreaModification

object SampleAreaModifications {
    const val MASK = 0x07
    const val GROUND = 0x1
    const val WATER = 0x2
    const val ROAD = 0x3
    const val DOOR = 0x4
    const val GRASS = 0x5
    const val JUMP = 0x6
    val AREAMOD_GROUND = AreaModification(GROUND, MASK)
    val AREAMOD_WATER = AreaModification(WATER, MASK)
    val AREAMOD_ROAD = AreaModification(ROAD, MASK)
    val AREAMOD_GRASS = AreaModification(GRASS, MASK)
    val AREAMOD_DOOR = AreaModification(DOOR, DOOR)
    val AREAMOD_JUMP = AreaModification(JUMP, JUMP)
    const val FLAGS_WALK = 0x01 // Ability to walk (ground, grass, road)
    const val FLAGS_SWIM = 0x02 // Ability to swim (water).
    const val FLAGS_DOOR = 0x04 // Ability to move through doors.
    const val FLAGS_JUMP = 0x08 // Ability to jump.
    const val FLAGS_DISABLED = 0x10 // Disabled polygon
    const val FLAGS_ALL = 0xffff // All abilities.
}