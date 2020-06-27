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

import org.ksdfv.thelema.gl.GL_LINES
import org.ksdfv.thelema.gl.GL_POINTS
import org.ksdfv.thelema.gl.GL_TRIANGLES
import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.shader.Shader


/** Renders points, lines, shape outlines and filled shapes.
 *
 *
 * By default a 2D orthographic projection with the origin in the lower left corner is used and units are specified in screen
 * pixels. This can be changed by configuring the projection matrix, usually using the [Camera.viewProjectionMatrix] matrix. If the
 * screen resolution changes, the projection matrix may need to be updated.
 *
 *
 * Shapes are rendered in batches to increase performance. Standard usage pattern looks as follows:
 *
 * <pre>
 * `camera.update();
 * shapeRenderer.setProjectionMatrix(camera.combined);
 *
 * shapeRenderer.begin(ShapeType.Line);
 * shapeRenderer.setColor(1, 1, 0, 1);
 * shapeRenderer.line(x, y, x2, y2);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.end();
 *
 * shapeRenderer.begin(ShapeType.Filled);
 * shapeRenderer.setColor(0, 1, 0, 1);
 * shapeRenderer.rect(x, y, width, height);
 * shapeRenderer.circle(x, y, radius);
 * shapeRenderer.end();
` *
</pre> *
 *
 * ShapeRenderer has a second matrix called the transformation matrix which is used to rotate, scale and translate shapes in a
 * more flexible manner. The following example shows how to rotate a rectangle around its center using the z-axis as the rotation
 * axis and placing it's center at (20, 12, 2):
 *
 * <pre>
 * shapeRenderer.begin(ShapeType.Line);
 * shapeRenderer.identity();
 * shapeRenderer.translate(20, 12, 2);
 * shapeRenderer.rotate(0, 0, 1, 90);
 * shapeRenderer.rect(-width / 2, -height / 2, width, height);
 * shapeRenderer.end();
</pre> *
 *
 * Matrix operations all use postmultiplication and work just like glTranslate, glScale and glRotate. The last transformation
 * specified will be the first that is applied to a shape (rotate then translate in the above example).
 *
 *
 * The projection and transformation matrices are a state of the ShapeRenderer, just like the color, and will be applied to all
 * shapes until they are changed.
 * @author mzechner, stbachmann, Nathan Sweet
 */
open class ShapeRenderer (maxVertices: Int = 5000, defaultShader: Shader? = null) {
    /** Shape types to be used with [.begin].
     * @author mzechner, stbachmann
     */
    enum class ShapeType(val glType: Int) {
        Point(GL_POINTS), Line(GL_LINES), Filled(GL_TRIANGLES);
    }

    var renderer: ImmediateModeRenderer = if (defaultShader == null) {
        ImmediateModeRenderer20(maxVertices, false, true, 0)
    } else {
        ImmediateModeRenderer20(maxVertices, false, true, 0, defaultShader)
    }

    private var matrixDirty = false

    /** If the matrix is modified, [.updateMatrices] must be called.
     * Sets the projection matrix to be used for rendering. Usually this will be set to [Camera.viewProjectionMatrix]. */
    var projectionMatrix: IMat4 = Mat4()
        set(value) {
            field.set(value)
            matrixDirty = true
        }

    /** If the matrix is modified, [.updateMatrices] must be called.  */
    var transformMatrix: IMat4 = Mat4()
        set(value) {
            field.set(value)
            matrixDirty = true
        }

    private val combinedMatrix: IMat4 = Mat4()
    private val tmp = Vec2()

    /** Sets the color to be used by the next shapes drawn.  */
    var color: IVec4 = Vec4(1f, 1f, 1f, 1f)
        set(value) {
            field.set(value)
        }
    /** Returns the current shape type.  */
    var currentType: ShapeType? = null
        private set

    /** If true, when drawing a shape cannot be performed with the current shape type, the batch is flushed and the shape type is
     * changed automatically. This can increase the number of batch flushes if care is not taken to draw the same type of shapes
     * together. Default is false.  */
    var autoShapeType = false

    private val defaultRectLineWidth = 0.75f

    /** Sets the color to be used by the next shapes drawn.  */
    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color.set(r, g, b, a)
    }

    fun updateMatrices() {
        matrixDirty = true
    }

    /** Sets the transformation matrix to identity.  */
    fun identity() {
        transformMatrix.idt()
        matrixDirty = true
    }

    /** Multiplies the current transformation matrix by a translation matrix.  */
    fun translate(x: Float, y: Float, z: Float) {
        transformMatrix.translate(x, y, z)
        matrixDirty = true
    }

    /** Multiplies the current transformation matrix by a rotation matrix.  */
    fun rotate(axisX: Float, axisY: Float, axisZ: Float, degrees: Float) {
        transformMatrix.rotate(axisX, axisY, axisZ, degrees)
        matrixDirty = true
    }

    /** Multiplies the current transformation matrix by a scale matrix.  */
    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float) {
        transformMatrix.scale(scaleX, scaleY, scaleZ)
        matrixDirty = true
    }

    fun render(shapeType: ShapeType, draw: ShapeRenderer.() -> Unit) {
        begin(shapeType)
        apply(draw)
        end()
    }

    fun renderLines(draw: ShapeRenderer.() -> Unit) = render(ShapeType.Line, draw)

    /** Begins a new batch without specifying a shape type.
     * @throws IllegalStateException if [.autoShapeType] is false.
     */
    fun begin() {
        check(autoShapeType) { "autoShapeType must be true to use this method." }
        begin(ShapeType.Line)
    }

    /** Starts a new batch of shapes. Shapes drawn within the batch will attempt to use the type specified. The call to this method
     * must be paired with a call to [.end].
     * @see .setAutoShapeType
     */
    fun begin(type: ShapeType?) {
        check(currentType == null) { "Call end() before beginning a new shape batch." }
        currentType = type
        if (matrixDirty) {
            combinedMatrix.set(projectionMatrix)
            combinedMatrix.mul(transformMatrix)
            matrixDirty = false
        }
        renderer.begin(combinedMatrix, currentType!!.glType)
    }

    fun set(type: ShapeType) {
        if (currentType == type) return
        checkNotNull(currentType) { "begin must be called first." }
        check(autoShapeType) { "autoShapeType must be enabled." }
        end()
        begin(type)
    }

    /** Draws a point using [ShapeType.Point], [ShapeType.Line] or [ShapeType.Filled].  */
    fun point(x: Float, y: Float, z: Float) {
        if (currentType == ShapeType.Line) {
            val size = defaultRectLineWidth * 0.5f
            line(x - size, y - size, z, x + size, y + size, z)
            return
        } else if (currentType == ShapeType.Filled) {
            val size = defaultRectLineWidth * 0.5f
            box(x - size, y - size, z - size, defaultRectLineWidth, defaultRectLineWidth, defaultRectLineWidth)
            return
        }
        check(ShapeType.Point, null, 1)
        renderer.color(color)
        renderer.vertex(x, y, z)
    }

    /** @see .line
     */
    fun line(v0: Vec3, v1: Vec3) {
        line(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, color, color)
    }

    /** @see .line
     */
    fun line(x: Float, y: Float, x2: Float, y2: Float) {
        line(x, y, 0.0f, x2, y2, 0.0f, color, color)
    }

    /** @see .line
     */
    fun line(v0: Vec2, v1: Vec2) {
        line(v0.x, v0.y, 0.0f, v1.x, v1.y, 0.0f, color, color)
    }

    /** @see .line
     */
    fun line(x: Float, y: Float, x2: Float, y2: Float, c1: IVec4, c2: IVec4) {
        line(x, y, 0.0f, x2, y2, 0.0f, c1, c2)
    }
    /** Draws a line using [ShapeType.Line] or [ShapeType.Filled]. The line is drawn with two colors interpolated
     * between the start and end points.  */
    /** Draws a line using [ShapeType.Line] or [ShapeType.Filled].  */
    
    fun line(x: Float, y: Float, z: Float, x2: Float, y2: Float, z2: Float, c1: IVec4 = color, c2: IVec4 = color) {
        if (currentType == ShapeType.Filled) {
            rectLine(x, y, x2, y2, defaultRectLineWidth, c1, c2)
            return
        }
        check(ShapeType.Line, null, 2)
        renderer.color(c1.r, c1.g, c1.b, c1.a)
        renderer.vertex(x, y, z)
        renderer.color(c2.r, c2.g, c2.b, c2.a)
        renderer.vertex(x2, y2, z2)
    }

    /** Draws a curve using [ShapeType.Line].  */
    fun curve(x1: Float, y1: Float, cx1: Float, cy1: Float, cx2: Float, cy2: Float, x2: Float, y2: Float, segments: Int) {
        var segments = segments
        check(ShapeType.Line, null, segments * 2 + 2)
        val colorBits = Color.toFloatBits(color)
        // Algorithm from: http://www.antigrain.com/research/bezier_interpolation/index.html#PAGE_BEZIER_INTERPOLATION
        val subdiv_step = 1f / segments
        val subdiv_step2 = subdiv_step * subdiv_step
        val subdiv_step3 = subdiv_step * subdiv_step * subdiv_step
        val pre1 = 3 * subdiv_step
        val pre2 = 3 * subdiv_step2
        val pre4 = 6 * subdiv_step2
        val pre5 = 6 * subdiv_step3
        val tmp1x = x1 - cx1 * 2 + cx2
        val tmp1y = y1 - cy1 * 2 + cy2
        val tmp2x = (cx1 - cx2) * 3 - x1 + x2
        val tmp2y = (cy1 - cy2) * 3 - y1 + y2
        var fx = x1
        var fy = y1
        var dfx = (cx1 - x1) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3
        var dfy = (cy1 - y1) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3
        var ddfx = tmp1x * pre4 + tmp2x * pre5
        var ddfy = tmp1y * pre4 + tmp2y * pre5
        val dddfx = tmp2x * pre5
        val dddfy = tmp2y * pre5
        while (segments-- > 0) {
            renderer.color(colorBits)
            renderer.vertex(fx, fy, 0f)
            fx += dfx
            fy += dfy
            dfx += ddfx
            dfy += ddfy
            ddfx += dddfx
            ddfy += dddfy
            renderer.color(colorBits)
            renderer.vertex(fx, fy, 0f)
        }
        renderer.color(colorBits)
        renderer.vertex(fx, fy, 0f)
        renderer.color(colorBits)
        renderer.vertex(x2, y2, 0f)
    }

    /** Draws a triangle in x/y plane using [ShapeType.Line] or [ShapeType.Filled].  */
    fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        check(ShapeType.Line, ShapeType.Filled, 6)
        val colorBits = Color.toFloatBits(color)
        if (currentType == ShapeType.Line) {
            renderer.color(colorBits)
            renderer.vertex(x1, y1, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2, y2, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2, y2, 0f)
            renderer.color(colorBits)
            renderer.vertex(x3, y3, 0f)
            renderer.color(colorBits)
            renderer.vertex(x3, y3, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1, y1, 0f)
        } else {
            renderer.color(colorBits)
            renderer.vertex(x1, y1, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2, y2, 0f)
            renderer.color(colorBits)
            renderer.vertex(x3, y3, 0f)
        }
    }

    /** Draws a triangle in x/y plane with colored corners using [ShapeType.Line] or [ShapeType.Filled].  */
    fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, col1: IVec4, col2: IVec4, col3: IVec4) {
        check(ShapeType.Line, ShapeType.Filled, 6)
        if (currentType == ShapeType.Line) {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
        } else {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
        }
    }

    /** Draws a rectangle in the x/y plane using [ShapeType.Line] or [ShapeType.Filled].  */
    fun rect(x: Float, y: Float, width: Float, height: Float) {
        check(ShapeType.Line, ShapeType.Filled, 8)
        val colorBits = Color.toFloatBits(color)
        if (currentType == ShapeType.Line) {
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
        } else {
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, 0f)
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
        }
    }

    /** Draws a rectangle in the x/y plane using [ShapeType.Line] or [ShapeType.Filled]. The x and y specify the lower
     * left corner.
     * @param col1 The color at (x, y).
     * @param col2 The color at (x + width, y).
     * @param col3 The color at (x + width, y + height).
     * @param col4 The color at (x, y + height).
     */
    fun rect(x: Float, y: Float, width: Float, height: Float, col1: IVec4, col2: IVec4, col3: IVec4, col4: IVec4) {
        check(ShapeType.Line, ShapeType.Filled, 8)
        if (currentType == ShapeType.Line) {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x, y, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x + width, y, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x + width, y, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x, y + height, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x, y + height, 0f)
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x, y, 0f)
        } else {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x, y, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x + width, y, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x + width, y + height, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x, y + height, 0f)
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x, y, 0f)
        }
    }
    /** Draws a rectangle in the x/y plane using [ShapeType.Line] or [ShapeType.Filled]. The x and y specify the lower
     * left corner. The originX and originY specify the point about which to rotate the rectangle.
     * @param col1 The color at (x, y)
     * @param col2 The color at (x + width, y)
     * @param col3 The color at (x + width, y + height)
     * @param col4 The color at (x, y + height)
     */
    /** Draws a rectangle in the x/y plane using [ShapeType.Line] or [ShapeType.Filled]. The x and y specify the lower
     * left corner. The originX and originY specify the point about which to rotate the rectangle.  */
    
    fun rect(x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float,
             degrees: Float, col1: IVec4 = color, col2: IVec4 = color, col3: IVec4 = color, col4: IVec4 = color) {
        check(ShapeType.Line, ShapeType.Filled, 8)
        val cos = MATH.cosDeg(degrees)
        val sin = MATH.sinDeg(degrees)
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        val x1 = cos * fx - sin * fy + worldOriginX
        val y1 = sin * fx + cos * fy + worldOriginY
        val x2 = cos * fx2 - sin * fy + worldOriginX
        val y2 = sin * fx2 + cos * fy + worldOriginY
        val x3 = cos * fx2 - sin * fy2 + worldOriginX
        val y3 = sin * fx2 + cos * fy2 + worldOriginY
        val x4 = x1 + (x3 - x2)
        val y4 = y3 - (y2 - y1)
        if (currentType == ShapeType.Line) {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x4, y4, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x4, y4, 0f)
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
        } else {
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
            renderer.color(col2.r, col2.g, col2.b, col2.a)
            renderer.vertex(x2, y2, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col3.r, col3.g, col3.b, col3.a)
            renderer.vertex(x3, y3, 0f)
            renderer.color(col4.r, col4.g, col4.b, col4.a)
            renderer.vertex(x4, y4, 0f)
            renderer.color(col1.r, col1.g, col1.b, col1.a)
            renderer.vertex(x1, y1, 0f)
        }
    }

    /** Draws a line using a rotated rectangle, where with one edge is centered at x1, y1 and the opposite edge centered at x2, y2.  */
    fun rectLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float) {
        var width = width
        check(ShapeType.Line, ShapeType.Filled, 8)
        val colorBits = Color.toFloatBits(color)
        val t = tmp.set(y2 - y1, x1 - x2).nor()
        width *= 0.5f
        val tx = t.x * width
        val ty = t.y * width
        if (currentType == ShapeType.Line) {
            renderer.color(colorBits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
        } else {
            renderer.color(colorBits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(colorBits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
        }
    }

    /** Draws a line using a rotated rectangle, where with one edge is centered at x1, y1 and the opposite edge centered at x2, y2.  */
    fun rectLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, c1: IVec4, c2: IVec4) {
        var width = width
        check(ShapeType.Line, ShapeType.Filled, 8)
        val col1Bits = Color.toFloatBits(c1)
        val col2Bits = Color.toFloatBits(c2)
        val t = tmp.set(y2 - y1, x1 - x2).nor()
        width *= 0.5f
        val tx = t.x * width
        val ty = t.y * width
        if (currentType == ShapeType.Line) {
            renderer.color(col1Bits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(col1Bits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(col1Bits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(col1Bits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
        } else {
            renderer.color(col1Bits)
            renderer.vertex(x1 + tx, y1 + ty, 0f)
            renderer.color(col1Bits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 - tx, y2 - ty, 0f)
            renderer.color(col2Bits)
            renderer.vertex(x2 + tx, y2 + ty, 0f)
            renderer.color(col1Bits)
            renderer.vertex(x1 - tx, y1 - ty, 0f)
        }
    }

    /** @see .rectLine
     */
    fun rectLine(p1: Vec2, p2: Vec2, width: Float) {
        rectLine(p1.x, p1.y, p2.x, p2.y, width)
    }

    /** Draws a cube using [ShapeType.Line] or [ShapeType.Filled]. The x, y and z specify the bottom, left, front corner
     * of the rectangle.  */
    fun box(x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float) {
        var depth = depth
        depth = -depth
        val colorBits = Color.toFloatBits(color)
        if (currentType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, 24)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
        } else {
            check(ShapeType.Line, ShapeType.Filled, 36)
            // Front
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            // Back
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
            // Left
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
            // Right
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            // Top
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z)
            renderer.color(colorBits)
            renderer.vertex(x + width, y + height, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x, y + height, z + depth)
            // Bottom
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + depth)
            renderer.color(colorBits)
            renderer.vertex(x + width, y, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
        }
    }

    /** Draws two crossed lines using [ShapeType.Line] or [ShapeType.Filled].  */
    fun x(x: Float, y: Float, size: Float) {
        line(x - size, y - size, x + size, y + size)
        line(x - size, y + size, x + size, y - size)
    }

    /** @see .x
     */
    fun x(p: Vec2, size: Float) {
        x(p.x, p.y, size)
    }
    /** Draws an arc using [ShapeType.Line] or [ShapeType.Filled].  */
    /** Calls [.arc] by estimating the number of segments needed for a smooth arc.  */
    fun arc(x: Float, y: Float, radius: Float, start: Float, degrees: Float, segments: Int = Math.max(1, (6 * Math.cbrt(radius.toDouble()).toFloat() * (degrees / 360.0f)).toInt())) {
        require(segments > 0) { "segments must be > 0." }
        val colorBits = Color.toFloatBits(color)
        val theta = 2 * MATH.PI * (degrees / 360.0f) / segments
        val cos = MATH.cos(theta)
        val sin = MATH.sin(theta)
        var cx = radius * MATH.cos(start * MATH.degreesToRadians)
        var cy = radius * MATH.sin(start * MATH.degreesToRadians)
        if (currentType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, segments * 2 + 2)
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, 0f)
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
                val temp = cx
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
            }
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, 0f)
        } else {
            check(ShapeType.Line, ShapeType.Filled, segments * 3 + 3)
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x, y, 0f)
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
                val temp = cx
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
            }
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, 0f)
        }
        val temp = cx
        cx = 0f
        cy = 0f
        renderer.color(colorBits)
        renderer.vertex(x + cx, y + cy, 0f)
    }
    /** Draws a circle using [ShapeType.Line] or [ShapeType.Filled].  */
    /** Calls [.circle] by estimating the number of segments needed for a smooth circle.  */
    
    fun circle(x: Float, y: Float, radius: Float, segments: Int = Math.max(1, (6 * Math.cbrt(radius.toDouble()).toFloat()).toInt())) {
        var segments = segments
        require(segments > 0) { "segments must be > 0." }
        val colorBits = Color.toFloatBits(color)
        val angle = 2 * MATH.PI / segments
        val cos = MATH.cos(angle)
        val sin = MATH.sin(angle)
        var cx = radius
        var cy = 0f
        if (currentType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, segments * 2 + 2)
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
                val temp = cx
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
            }
            // Ensure the last segment is identical to the first.
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, 0f)
        } else {
            check(ShapeType.Line, ShapeType.Filled, segments * 3 + 3)
            segments--
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x, y, 0f)
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
                val temp = cx
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, 0f)
            }
            // Ensure the last segment is identical to the first.
            renderer.color(colorBits)
            renderer.vertex(x, y, 0f)
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, 0f)
        }
        val temp = cx
        cx = radius
        cy = 0f
        renderer.color(colorBits)
        renderer.vertex(x + cx, y + cy, 0f)
    }
    /** Draws an ellipse using [ShapeType.Line] or [ShapeType.Filled].  */
    /** Calls [.ellipse] by estimating the number of segments needed for a smooth ellipse.  */
    fun ellipse(x: Float, y: Float, width: Float, height: Float, segments: Int = Math.max(1, (12 * Math.cbrt(Math.max(width * 0.5f, height * 0.5f).toDouble()).toFloat()).toInt())) {
        require(segments > 0) { "segments must be > 0." }
        check(ShapeType.Line, ShapeType.Filled, segments * 3)
        val colorBits = Color.toFloatBits(color)
        val angle = 2 * MATH.PI / segments
        val cx = x + width * 0.5f
        val cy = y + height * 0.5f
        if (currentType == ShapeType.Line) {
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(cx + width * 0.5f * MATH.cos(i * angle), cy + height * 0.5f * MATH.sin(i * angle), 0f)
                renderer.color(colorBits)
                renderer.vertex(cx + width * 0.5f * MATH.cos((i + 1) * angle),
                        cy + height * 0.5f * MATH.sin((i + 1) * angle), 0f)
            }
        } else {
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(cx + width * 0.5f * MATH.cos(i * angle), cy + height * 0.5f * MATH.sin(i * angle), 0f)
                renderer.color(colorBits)
                renderer.vertex(cx, cy, 0f)
                renderer.color(colorBits)
                renderer.vertex(cx + width * 0.5f * MATH.cos((i + 1) * angle),
                        cy + height * 0.5f * MATH.sin((i + 1) * angle), 0f)
            }
        }
    }
    /** Draws an ellipse using [ShapeType.Line] or [ShapeType.Filled].  */
    /** Calls [.ellipse] by estimating the number of segments needed for a smooth ellipse.  */
    fun ellipse(x: Float, y: Float, width: Float, height: Float, rotation: Float, segments: Int = Math.max(1, (12 * Math.cbrt(Math.max(width * 0.5f, height * 0.5f).toDouble()).toFloat()).toInt())) {
        var rotation = rotation
        require(segments > 0) { "segments must be > 0." }
        check(ShapeType.Line, ShapeType.Filled, segments * 3)
        val colorBits = Color.toFloatBits(color)
        val angle = 2 * MATH.PI / segments
        rotation = MATH.PI * rotation / 180f
        val sin = MATH.sin(rotation)
        val cos = MATH.cos(rotation)
        val cx = x + width * 0.5f
        val cy = y + height * 0.5f
        var x1 = width * 0.5f
        var y1 = 0f
        if (currentType == ShapeType.Line) {
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0f)
                x1 = width * 0.5f * MATH.cos((i + 1) * angle)
                y1 = height * 0.5f * MATH.sin((i + 1) * angle)
                renderer.color(colorBits)
                renderer.vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0f)
            }
        } else {
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0f)
                renderer.color(colorBits)
                renderer.vertex(cx, cy, 0f)
                x1 = width * 0.5f * MATH.cos((i + 1) * angle)
                y1 = height * 0.5f * MATH.sin((i + 1) * angle)
                renderer.color(colorBits)
                renderer.vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0f)
            }
        }
    }
    /** Draws a cone using [ShapeType.Line] or [ShapeType.Filled].  */
    /** Calls [.cone] by estimating the number of segments needed for a smooth
     * circular base.  */
    
    fun cone(x: Float, y: Float, z: Float, radius: Float, height: Float, segments: Int = Math.max(1, (4 * Math.sqrt(radius.toDouble()).toFloat()).toInt())) {
        var segments = segments
        require(segments > 0) { "segments must be > 0." }
        check(ShapeType.Line, ShapeType.Filled, segments * 4 + 2)
        val colorBits = Color.toFloatBits(color)
        val angle = 2 * MATH.PI / segments
        val cos = MATH.cos(angle)
        val sin = MATH.sin(angle)
        var cx = radius
        var cy = 0f
        if (currentType == ShapeType.Line) {
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
                renderer.color(colorBits)
                renderer.vertex(x, y, z + height)
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
                val temp = cx
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
            }
            // Ensure the last segment is identical to the first.
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, z)
        } else {
            segments--
            for (i in 0 until segments) {
                renderer.color(colorBits)
                renderer.vertex(x, y, z)
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
                val temp = cx
                val temp2 = cy
                cx = cos * cx - sin * cy
                cy = sin * temp + cos * cy
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
                renderer.color(colorBits)
                renderer.vertex(x + temp, y + temp2, z)
                renderer.color(colorBits)
                renderer.vertex(x + cx, y + cy, z)
                renderer.color(colorBits)
                renderer.vertex(x, y, z + height)
            }
            // Ensure the last segment is identical to the first.
            renderer.color(colorBits)
            renderer.vertex(x, y, z)
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, z)
        }
        val temp = cx
        val temp2 = cy
        cx = radius
        cy = 0f
        renderer.color(colorBits)
        renderer.vertex(x + cx, y + cy, z)
        if (currentType != ShapeType.Line) {
            renderer.color(colorBits)
            renderer.vertex(x + temp, y + temp2, z)
            renderer.color(colorBits)
            renderer.vertex(x + cx, y + cy, z)
            renderer.color(colorBits)
            renderer.vertex(x, y, z + height)
        }
    }
    /** Draws a polygon in the x/y plane using [ShapeType.Line]. The vertices must contain at least 3 points (6 floats x,y).  */
    /** @see .polygon
     */
    fun polygon(vertices: FloatArray, offset: Int = 0, count: Int = vertices.size) {
        require(count >= 6) { "Polygons must contain at least 3 points." }
        require(count % 2 == 0) { "Polygons must have an even number of vertices." }
        check(ShapeType.Line, null, count)
        val colorBits = Color.toFloatBits(color)
        val firstX = vertices[0]
        val firstY = vertices[1]
        var i = offset
        val n = offset + count
        while (i < n) {
            val x1 = vertices[i]
            val y1 = vertices[i + 1]
            var x2: Float
            var y2: Float
            if (i + 2 >= count) {
                x2 = firstX
                y2 = firstY
            } else {
                x2 = vertices[i + 2]
                y2 = vertices[i + 3]
            }
            renderer.color(colorBits)
            renderer.vertex(x1, y1, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2, y2, 0f)
            i += 2
        }
    }
    /** Draws a polyline in the x/y plane using [ShapeType.Line]. The vertices must contain at least 2 points (4 floats x,y).  */
    /** @see .polyline
     */
    fun polyline(vertices: FloatArray, offset: Int = 0, count: Int = vertices.size) {
        require(count >= 4) { "Polylines must contain at least 2 points." }
        require(count % 2 == 0) { "Polylines must have an even number of vertices." }
        check(ShapeType.Line, null, count)
        val colorBits = Color.toFloatBits(color)
        var i = offset
        val n = offset + count - 2
        while (i < n) {
            val x1 = vertices[i]
            val y1 = vertices[i + 1]
            val x2: Float = vertices[i + 2]
            val y2: Float = vertices[i + 3]
            renderer.color(colorBits)
            renderer.vertex(x1, y1, 0f)
            renderer.color(colorBits)
            renderer.vertex(x2, y2, 0f)
            i += 2
        }
    }

    /** @param other May be null.
     */
    private fun check(preferred: ShapeType, other: ShapeType?, newVertices: Int) {
        checkNotNull(currentType) { "begin must be called first." }
        if (currentType != preferred && currentType != other) { // Shape type is not valid.
            if (!autoShapeType) {
                checkNotNull(other) { "Must call begin(ShapeType.$preferred)." }
                throw IllegalStateException("Must call begin(ShapeType.$preferred) or begin(ShapeType.$other).")
            }
            end()
            begin(preferred)
        } else if (matrixDirty) { // Matrix has been changed.
            val type = currentType!!
            end()
            begin(type)
        } else if (renderer.maxVertices - renderer.numVertices < newVertices) { // Not enough space.
            val type = currentType!!
            end()
            begin(type)
        }
    }

    /** Finishes the batch of shapes and ensures they get rendered.  */
    fun end() {
        renderer.end()
        currentType = null
    }

    fun flush() {
        val type = currentType ?: return
        end()
        begin(type)
    }

    /** @return true if currently between begin and end.
     */
    val isDrawing: Boolean
        get() = currentType != null

    fun dispose() {
        renderer.dispose()
    }

    init {
        projectionMatrix.setToOrtho(0f, 0f, APP.width.toFloat(), APP.height.toFloat())
        matrixDirty = true
    }
}
