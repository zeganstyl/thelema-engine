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

package org.ksdfv.thelema


enum class HdpiMode {
    /**
     * mouse coordinates, [Graphics.getWidth] and
     * [Graphics.getHeight] will return logical coordinates
     * according to the system defined HDPI scaling. Rendering will be
     * performed to a backbuffer at raw resolution. Use [HdpiUtils]
     * when calling glScissor or glViewport which
     * expect raw coordinates.
     */
    Logical,
    /**
     * Mouse coordinates, [Graphics.getWidth] and
     * [Graphics.getHeight] will return raw pixel coordinates
     * irrespective of the system defined HDPI scaling.
     */
    Pixels
}
