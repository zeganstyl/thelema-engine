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

package app.thelema.g3d.terrain

interface TerrainListener {
    /** You can here, for example, setup shader.
     * @param level level of detail corresponding to this tile
     * @param tileX tile start X position
     * @param tileZ tile start Z position */
    fun beforeTileRender(level: TerrainLevel, tileX: Float, tileZ: Float) {}

    /** Called when tile position is changed on camera moving.
     * @param level level of detail corresponding to this tile
     * @param oldIndexX old index X of tile
     * @param oldIndexZ old index Z of tile
     * @param oldX tile old start X position
     * @param oldZ tile old start Z position */
    fun tileIndexChanged(level: TerrainLevel, oldIndexX: Int, oldIndexZ: Int, oldX: Float, oldZ: Float) {}
}