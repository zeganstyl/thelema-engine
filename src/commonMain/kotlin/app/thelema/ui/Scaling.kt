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

/** Various scaling types for fitting one rectangle into another.
 * @author Nathan Sweet
 */
enum class Scaling {
    /** Scales the source to fit the target while keeping the same aspect ratio. This may cause the source to be smaller than the
     * target in one direction.  */
    fit,
    /** Scales the source to fill the target while keeping the same aspect ratio. This may cause the source to be larger than the
     * target in one direction.  */
    fill,
    /** Scales the source to fill the target in the x direction while keeping the same aspect ratio. This may cause the source to be
     * smaller or larger than the target in the y direction.  */
    fillX,
    /** Scales the source to fill the target in the y direction while keeping the same aspect ratio. This may cause the source to be
     * smaller or larger than the target in the x direction.  */
    fillY,
    /** Scales the source to fill the target. This may cause the source to not keep the same aspect ratio.  */
    stretch,
    /** Scales the source to fill the target in the x direction, without changing the y direction. This may cause the source to not
     * keep the same aspect ratio.  */
    stretchX,
    /** Scales the source to fill the target in the y direction, without changing the x direction. This may cause the source to not
     * keep the same aspect ratio.  */
    stretchY,
    /** The source is not scaled.  */
    none;

    /** Returns the size of the source scaled to the target. Note the same Vector2 instance is always returned and should never be
     * cached.  */
    fun apply(sourceWidth: Float, sourceHeight: Float, targetWidth: Float, targetHeight: Float, result: (x: Float, y: Float) -> Unit) {
        when (this) {
            fit -> {
                val targetRatio = targetHeight / targetWidth
                val sourceRatio = sourceHeight / sourceWidth
                val scale = if (targetRatio > sourceRatio) targetWidth / sourceWidth else targetHeight / sourceHeight
                result(sourceWidth * scale, sourceHeight * scale)
            }
            fill -> {
                val targetRatio = targetHeight / targetWidth
                val sourceRatio = sourceHeight / sourceWidth
                val scale = if (targetRatio < sourceRatio) targetWidth / sourceWidth else targetHeight / sourceHeight
                result(sourceWidth * scale, sourceHeight * scale)
            }
            fillX -> {
                val scale = targetWidth / sourceWidth
                result(sourceWidth * scale, sourceHeight * scale)
            }
            fillY -> {
                val scale = targetHeight / sourceHeight
                result(sourceWidth * scale, sourceHeight * scale)
            }
            stretch -> result(targetWidth, targetHeight)
            stretchX -> result(targetWidth, sourceHeight)
            stretchY -> result(sourceWidth, targetHeight)
            none -> result(sourceWidth, sourceHeight)
        }
    }
}
