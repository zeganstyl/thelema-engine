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

import kotlin.math.max
import kotlin.math.min

/** Encapsulates a 2D rectangle defined by its corner point in the bottom left and its extents in x (width) and y (height).
 * @author badlogicgames@gmail.com, zeganstyl
 */
interface IRectangle {
    var x: Float
    var y: Float
    var width: Float
    var height: Float

    /** @param x bottom-left x coordinate
     * @param y bottom-left y coordinate
     * @param width width
     * @param height height
     * @return this rectangle for chaining
     */
    fun set(x: Float, y: Float, width: Float, height: Float): IRectangle {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        return this
    }

    fun set(other: Rectangle): IRectangle {
        x = other.x
        y = other.y
        width = other.width
        height = other.height
        return this
    }

    /** Sets the x and y-coordinates of the bottom left corner from vector
     * @param position The position vector
     * @return this rectangle for chaining
     */
    fun setPosition(position: IVec2): IRectangle {
        x = position.x
        y = position.y
        return this
    }

    /** Sets the x and y-coordinates of the bottom left corner
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return this rectangle for chaining
     */
    fun setPosition(x: Float, y: Float): IRectangle {
        this.x = x
        this.y = y
        return this
    }

    /** Sets the width and height of this rectangle
     * @param width The width
     * @param height The height
     * @return this rectangle for chaining
     */
    fun setSize(width: Float, height: Float): IRectangle {
        this.width = width
        this.height = height
        return this
    }

    /** Sets the squared size of this rectangle
     * @param sizeXY The size
     * @return this rectangle for chaining
     */
    fun setSize(sizeXY: Float): IRectangle {
        width = sizeXY
        height = sizeXY
        return this
    }

    /** @return whether the point (x, y) is contained in the rectangle */
    fun contains(x: Float, y: Float): Boolean {
        return this.x <= x && this.x + width >= x && this.y <= y && this.y + height >= y
    }

    /** @return whether the point is contained in the rectangle */
    fun contains(point: IVec2): Boolean = contains(point.x, point.y)

    /** @param rectangle the other [Rectangle].
     * @return whether the other rectangle is contained in this rectangle.
     */
    operator fun contains(rectangle: Rectangle): Boolean {
        val xmin = rectangle.x
        val xmax = xmin + rectangle.width
        val ymin = rectangle.y
        val ymax = ymin + rectangle.height
        return (xmin > x && xmin < x + width && xmax > x && xmax < x + width
                && ymin > y && ymin < y + height && ymax > y && ymax < y + height)
    }

    /** @param r the other [Rectangle]
     * @return whether this rectangle overlaps the other rectangle.
     */
    fun overlaps(r: Rectangle): Boolean =
        x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y

    /** Merges this rectangle with the other rectangle. The rectangle should not have negative width or negative height.
     * @param rect the other rectangle
     * @return this rectangle for chaining
     */
    fun merge(rect: Rectangle): IRectangle {
        val minX = min(x, rect.x)
        val maxX = max(x + width, rect.x + rect.width)
        x = minX
        width = maxX - minX
        val minY = min(y, rect.y)
        val maxY = max(y + height, rect.y + rect.height)
        y = minY
        height = maxY - minY
        return this
    }

    /** Merges this rectangle with a point (x, y).
     * The rectangle should not have negative width or negative height. */
    fun merge(x: Float, y: Float): IRectangle {
        val minX = min(this.x, x)
        val maxX = max(this.x + width, x)
        this.x = minX
        width = maxX - minX
        val minY = min(this.y, y)
        val maxY = max(this.y + height, y)
        this.y = minY
        height = maxY - minY
        return this
    }

    /** Merges this rectangle with a point. The rectangle should not have negative width or negative height. */
    fun merge(point: Vec2): IRectangle = merge(point.x, point.y)

    /** Merges this rectangle with a list of points. The rectangle should not have negative width or negative height. */
    fun merge(points: List<Vec2>): IRectangle {
        var minX = x
        var maxX = x + width
        var minY = y
        var maxY = y + height
        for (i in points.indices) {
            val v = points[i]
            minX = min(minX, v.x)
            maxX = max(maxX, v.x)
            minY = min(minY, v.y)
            maxY = max(maxY, v.y)
        }
        x = minX
        width = maxX - minX
        y = minY
        height = maxY - minY
        return this
    }

    /** Calculates the aspect ratio ( width / height ) of this rectangle
     * @return the aspect ratio of this rectangle. Returns Float.NaN if height is 0 to avoid ArithmeticException
     */
    val aspectRatio: Float
        get() = if (height == 0f) Float.NaN else width / height

    /** Calculates the center of the rectangle. Results are located in the given Vector2 */
    fun getCenter(vector: IVec2): IVec2 {
        vector.x = x + width * 0.5f
        vector.y = y + height * 0.5f
        return vector
    }

    /** Moves this rectangle so that its center point is located at a given position (x, y) */
    fun setCenter(x: Float, y: Float): IRectangle {
        setPosition(x - width * 0.5f, y - height * 0.5f)
        return this
    }

    /** Moves this rectangle so that its center point is located at a given position
     * @param position the position
     * @return this for chaining
     */
    fun setCenter(position: Vec2): IRectangle {
        setPosition(position.x - width / 2, position.y - height / 2)
        return this
    }

    /** Fits this rectangle around another rectangle while maintaining aspect ratio. This scales and centers the rectangle to the
     * other rectangle (e.g. Having a camera translate and scale to show a given area)
     * @param rect the other rectangle to fit this rectangle around
     * @return this rectangle for chaining
     */
    fun fitOutside(rect: Rectangle): IRectangle {
        val ratio = aspectRatio
        if (ratio > rect.aspectRatio) { // Wider than tall
            setSize(rect.height * ratio, rect.height)
        } else { // Taller than wide
            setSize(rect.width, rect.width / ratio)
        }
        setPosition(rect.x + rect.width / 2 - width / 2, rect.y + rect.height / 2 - height / 2)
        return this
    }

    /** Fits this rectangle into another rectangle while maintaining aspect ratio. This scales and centers the rectangle to the
     * other rectangle (e.g. Scaling a texture within a arbitrary cell without squeezing)
     * @param rect the other rectangle to fit this rectangle inside
     * @return this rectangle for chaining
     */
    fun fitInside(rect: Rectangle): IRectangle {
        val ratio = aspectRatio
        if (ratio < rect.aspectRatio) { // Taller than wide
            setSize(rect.height * ratio, rect.height)
        } else { // Wider than tall
            setSize(rect.width, rect.width / ratio)
        }
        setPosition(rect.x + rect.width / 2 - width / 2, rect.y + rect.height / 2 - height / 2)
        return this
    }

    fun perimeter(): Float = 2 * (width + height)
}