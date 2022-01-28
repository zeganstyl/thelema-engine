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

package app.thelema.g2d

import app.thelema.app.APP
import app.thelema.g2d.SpriteBatch.Companion.createDefaultShader
import app.thelema.gl.*
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.img.ITexture2D
import app.thelema.utils.Color

/** Draws batched quads using indices.
 *
 * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
 * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
 * respect to the current screen resolution.
 *
 *
 * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
 * the ones expect for shaders set with [shader]. See [createDefaultShader].
 * @param size The max number of sprites in a single batch. If -1 (by default), mesh will be auto resized if needed.
 * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
 *
 * @author mzechner, Nathan Sweet, zeganstyl
 */
open class SpriteBatch (size: Int = -1, defaultShader: Shader = createDefaultShader(), var ownsShader: Boolean = true) : Batch {
    var autoResize: Boolean = size == -1

    val verts = VertexBuffer {
        addAttribute(2, "POSITION", GL_FLOAT, false)
        addAttribute(4, "COLOR", GL_UNSIGNED_BYTE, true)
        addAttribute(2, "UV", GL_FLOAT, false)
        initVertexBuffer(if (autoResize) DEFAULT_VERTICES_NUM else size)
    }

    var vertices = verts.bytes.floatView()

    override val vertexBuffer: IVertexBuffer
        get() = verts

    private val mesh = Mesh().apply {
        getOrCreateEntity().name = "Sprite Batch"
        addVertexBuffer(verts)

        setIndexBuffer {
            resizeIndexBuffer(this, (if (autoResize) DEFAULT_VERTICES_NUM else size) * 6)
        }

//        indices = IndexBuffer(DATA.bytes(size * 6 * 2).apply {
//            shortView().apply {
//                val len = size * 6
//                var j = 0
//                var i = 0
//                while (i < len) {
//                    this[i] = j.toShort()
//                    this[i + 1] = (j + 1).toShort()
//                    this[i + 2] = (j + 2).toShort()
//                    this[i + 3] = (j + 2).toShort()
//                    this[i + 4] = (j + 3).toShort()
//                    this[i + 5] = this[i]
//                    i += 6
//                    j += 4
//                }
//            }
//        }).apply { indexType = GL_UNSIGNED_SHORT }
    }

    var idx = 0
    var lastTexture: ITexture2D? = null
    var invTexWidth = 0f
    var invTexHeight = 0f
    var drawing = false
    override var transformMatrix: IMat4 = Mat4()
        set(value) {
            if (drawing) flush()
            field.set(value)
            if (drawing) setupMatrices()
        }
    override var projectionMatrix = Mat4().setToOrtho(0f, APP.width.toFloat(), 0f, APP.height.toFloat())
        set(value) {
            if (drawing) flush()
            field.set(value)
            if (drawing) setupMatrices()
        }
    override val combinedMatrix = Mat4()
    private var blendingDisabled = false
    override var shader: IShader = defaultShader

    override var color: Int = 0xFFFFFFFF.toInt()

    override val isDrawing: Boolean
        get() = drawing

    override var packedColor: Float = Color.toFloatBits(1f, 1f, 1f, 1f)

    /** Number of render calls since the last [begin].  */
    var renderCalls = 0
    /** Number of rendering calls, ever. Will not be reset unless set manually.  */
    var totalRenderCalls = 0
    /** The maximum number of sprites rendered in one batch so far.  */
    var maxSpritesInBatch = 0

    private fun resizeIndexBuffer(buffer: IIndexBuffer, spritesNum: Int) {
        buffer.initIndexBuffer(spritesNum * 6) {
            val len = count
            var j = 0
            var i = 0
            while (i < len) {
                putIndex(j)
                putIndex(j + 1)
                putIndex(j + 2)
                putIndex(j + 2)
                putIndex(j + 3)
                putIndex(j)
                i += 6
                j += 4
            }
        }
    }

    override fun begin() {
        check(!drawing) { "SpriteBatch.end must be called before begin." }
        renderCalls = 0
//        depthMaskTemp = GL.isDepthMaskEnabled
//        blendingTemp = GL.isBlendingEnabled
        shader.bind()
        setupMatrices()
        drawing = true
    }

    override fun end() {
        check(drawing) { "SpriteBatch.begin must be called before end." }
        if (idx > 0) flush()
        lastTexture = null
        drawing = false
//        GL.isDepthMaskEnabled = depthMaskTemp
//        GL.isBlendingEnabled = blendingTemp
    }

    override fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color = Color.toIntBits(r, g, b, a)
    }

    private fun resizeMesh(requiredFloats: Int = idx) {
        if (autoResize && requiredFloats >= vertices.capacity) {
            var floatsNum = vertices.capacity
            while (floatsNum < requiredFloats) {
                floatsNum *= 2
            }
            verts.initVertexBuffer(floatsNum / 5)
            vertices = verts.bytes.floatView()
            resizeIndexBuffer(mesh.indices!!, floatsNum / 20)
        }
    }

    override fun draw(texture: ITexture2D, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int, flipX: Boolean, flipY: Boolean) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY
        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }
        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy
        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float
        // rotate
        if (rotation != 0f) {
            val cos = MATH.cos(rotation)
            val sin = MATH.sin(rotation)
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y
            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
            x4 = p4x
            y4 = p4y
        }
        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY
        var u = srcX * invTexWidth
        var v = (srcY + srcHeight) * invTexHeight
        var u2 = (srcX + srcWidth) * invTexWidth
        var v2 = srcY * invTexHeight
        if (flipX) {
            val tmp = u
            u = u2
            u2 = tmp
        }
        if (flipY) {
            val tmp = v
            v = v2
            v2 = tmp
        }
        val color = this.packedColor
        val idx = idx
        vertices[idx] = x1
        vertices[idx + 1] = y1
        vertices[idx + 2] = color
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 5] = x2
        vertices[idx + 6] = y2
        vertices[idx + 7] = color
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 10] = x3
        vertices[idx + 11] = y3
        vertices[idx + 12] = color
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 15] = x4
        vertices[idx + 16] = y4
        vertices[idx + 17] = color
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        this.idx = idx + 20
    }

    override fun draw(texture: ITexture2D, x: Float, y: Float, width: Float, height: Float, srcX: Int, srcY: Int, srcWidth: Int,
                      srcHeight: Int, flipX: Boolean, flipY: Boolean) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        var u = srcX * invTexWidth
        var v = (srcY + srcHeight) * invTexHeight
        var u2 = (srcX + srcWidth) * invTexWidth
        var v2 = srcY * invTexHeight
        val fx2 = x + width
        val fy2 = y + height
        if (flipX) {
            val tmp = u
            u = u2
            u2 = tmp
        }
        if (flipY) {
            val tmp = v
            v = v2
            v2 = tmp
        }
        val color = this.packedColor
        val idx = idx
        vertices[idx] = x
        vertices[idx + 1] = y
        vertices[idx + 2] = color
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 5] = x
        vertices[idx + 6] = fy2
        vertices[idx + 7] = color
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 10] = fx2
        vertices[idx + 11] = fy2
        vertices[idx + 12] = color
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 15] = fx2
        vertices[idx + 16] = y
        vertices[idx + 17] = color
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        this.idx = idx + 20
    }

    override fun draw(texture: ITexture2D, x: Float, y: Float, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) {
        val u = srcX * invTexWidth
        val v = (srcY + srcHeight) * invTexHeight
        val u2 = (srcX + srcWidth) * invTexWidth
        val v2 = srcY * invTexHeight
        val fx2 = x + srcWidth
        val fy2 = y + srcHeight
        draw(texture, x, y, fx2, fy2, u, v, u2, v2, color)
    }

    override fun draw(texture: ITexture2D, x: Float, y: Float, width: Float, height: Float, u: Float, v: Float, u2: Float, v2: Float) =
        draw(texture, x, y, x + width, y + height, u, v, u2, v2, color)

    override fun draw(
        texture: ITexture2D,
        x: Float,
        y: Float,
        x2: Float,
        y2: Float,
        u: Float,
        v: Float,
        u2: Float,
        v2: Float,
        color: Int
    ) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        val idx = idx
        val bytes = verts.bytes
        val byteOffset = idx * 4
        vertices[idx] = x
        vertices[idx + 1] = y
        bytes.putRGBA(byteOffset + 8, color)
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 5] = x
        vertices[idx + 6] = y2
        bytes.putRGBA(byteOffset + 28, color)
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 10] = x2
        vertices[idx + 11] = y2
        bytes.putRGBA(byteOffset + 48, color)
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 15] = x2
        vertices[idx + 16] = y
        bytes.putRGBA(byteOffset + 68, color)
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        this.idx += 20
    }

    override fun draw(texture: ITexture2D, x: Float, y: Float, width: Float, height: Float) =
        draw(texture, x, y, x + width, y + height, 0f, 1f, 1f, 0f, color)

    override fun draw(texture: ITexture2D, spriteVertices: FloatArray, offset: Int, count: Int) {
        switchTexture(texture)
        resizeMesh(idx + spriteVertices.size)
        vertices.position = idx
        vertices.put(spriteVertices, count, offset)
        idx += count
    }

    override fun draw(region: TextureRegion, x: Float, y: Float, width: Float, height: Float) =
        draw(region.texture, x, y, x + width, y + height, region.u, region.v2, region.u2, region.v, color)

    override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
                      scaleX: Float, scaleY: Float, rotation: Float) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        val texture = region.texture
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY
        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }
        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy
        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float
        // rotate
        if (rotation != 0f) {
            val cos = MATH.cos(rotation)
            val sin = MATH.sin(rotation)
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y
            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
            x4 = p4x
            y4 = p4y
        }
        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY
        val u = region.u
        val v = region.v2
        val u2 = region.u2
        val v2 = region.v
        val color = this.packedColor
        val idx = idx
        vertices[idx] = x1
        vertices[idx + 1] = y1
        vertices[idx + 2] = color
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 5] = x2
        vertices[idx + 6] = y2
        vertices[idx + 7] = color
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 10] = x3
        vertices[idx + 11] = y3
        vertices[idx + 12] = color
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 15] = x4
        vertices[idx + 16] = y4
        vertices[idx + 17] = color
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        this.idx = idx + 20
    }

    override fun draw(region: TextureRegion, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float,
                      scaleX: Float, scaleY: Float, rotation: Float, clockwise: Boolean) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        val texture = region.texture
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        // bottom left and top right corner points relative to origin
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        var fx = -originX
        var fy = -originY
        var fx2 = width - originX
        var fy2 = height - originY
        // scale
        if (scaleX != 1f || scaleY != 1f) {
            fx *= scaleX
            fy *= scaleY
            fx2 *= scaleX
            fy2 *= scaleY
        }
        // construct corner points, start from top left and go counter clockwise
        val p1x = fx
        val p1y = fy
        val p2x = fx
        val p2y = fy2
        val p3x = fx2
        val p3y = fy2
        val p4x = fx2
        val p4y = fy
        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float
        // rotate
        if (rotation != 0f) {
            val cos = MATH.cos(rotation)
            val sin = MATH.sin(rotation)
            x1 = cos * p1x - sin * p1y
            y1 = sin * p1x + cos * p1y
            x2 = cos * p2x - sin * p2y
            y2 = sin * p2x + cos * p2y
            x3 = cos * p3x - sin * p3y
            y3 = sin * p3x + cos * p3y
            x4 = x1 + (x3 - x2)
            y4 = y3 - (y2 - y1)
        } else {
            x1 = p1x
            y1 = p1y
            x2 = p2x
            y2 = p2y
            x3 = p3x
            y3 = p3y
            x4 = p4x
            y4 = p4y
        }
        x1 += worldOriginX
        y1 += worldOriginY
        x2 += worldOriginX
        y2 += worldOriginY
        x3 += worldOriginX
        y3 += worldOriginY
        x4 += worldOriginX
        y4 += worldOriginY
        val u1: Float
        val v1: Float
        val u2: Float
        val v2: Float
        val u3: Float
        val v3: Float
        val u4: Float
        val v4: Float
        if (clockwise) {
            u1 = region.u2
            v1 = region.v2
            u2 = region.u
            v2 = region.v2
            u3 = region.u
            v3 = region.v
            u4 = region.u2
            v4 = region.v
        } else {
            u1 = region.u
            v1 = region.v
            u2 = region.u2
            v2 = region.v
            u3 = region.u2
            v3 = region.v2
            u4 = region.u
            v4 = region.v2
        }
        val color = this.packedColor
        val idx = idx
        vertices[idx] = x1
        vertices[idx + 1] = y1
        vertices[idx + 2] = color
        vertices[idx + 3] = u1
        vertices[idx + 4] = v1
        vertices[idx + 5] = x2
        vertices[idx + 6] = y2
        vertices[idx + 7] = color
        vertices[idx + 8] = u2
        vertices[idx + 9] = v2
        vertices[idx + 10] = x3
        vertices[idx + 11] = y3
        vertices[idx + 12] = color
        vertices[idx + 13] = u3
        vertices[idx + 14] = v3
        vertices[idx + 15] = x4
        vertices[idx + 16] = y4
        vertices[idx + 17] = color
        vertices[idx + 18] = u4
        vertices[idx + 19] = v4
        this.idx = idx + 20
    }

    override fun draw(region: TextureRegion, width: Float, height: Float, transform: Affine2) {
        check(drawing) { "SpriteBatch.begin must be called before draw." }
        val vertices = vertices
        val texture = region.texture
        if (texture !== lastTexture) switchTexture(texture) else resizeMesh()
        // construct corner points
        val x1 = transform.m02
        val y1 = transform.m12
        val x2 = transform.m01 * height + transform.m02
        val y2 = transform.m11 * height + transform.m12
        val x3 = transform.m00 * width + transform.m01 * height + transform.m02
        val y3 = transform.m10 * width + transform.m11 * height + transform.m12
        val x4 = transform.m00 * width + transform.m02
        val y4 = transform.m10 * width + transform.m12
        val u = region.u
        val v = region.v2
        val u2 = region.u2
        val v2 = region.v
        val color = this.packedColor
        val idx = idx

        vertices[idx] = x1
        vertices[idx + 1] = y1
        vertices[idx + 2] = color
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 5] = x2
        vertices[idx + 6] = y2
        vertices[idx + 7] = color
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 10] = x3
        vertices[idx + 11] = y3
        vertices[idx + 12] = color
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 15] = x4
        vertices[idx + 16] = y4
        vertices[idx + 17] = color
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        this.idx = idx + 20
    }

    override fun flush() {
        if (idx == 0) return
        renderCalls++
        totalRenderCalls++
        val spritesInBatch = idx / 20
        if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch
        val count = spritesInBatch * 6
        lastTexture?.bind(0)
        val mesh = mesh
        verts.bytes.limit = idx * 4
        vertices.limit = idx
        verts.gpuUploadRequested = true

        GL.isCullFaceEnabled = false
        GL.isDepthMaskEnabled = true

        if (blendingDisabled) {
            GL.isBlendingEnabled = false
        } else {
            GL.isBlendingEnabled = true
            GL.setupSimpleAlphaBlending()
        }

        mesh.verticesCount = spritesInBatch * 4
        mesh.render(shader, null,0, count)
        idx = 0

        verts.bytes.limit = verts.bytes.capacity
        vertices.limit = vertices.capacity
    }

    override fun disableBlending() {
        if (blendingDisabled) return
        flush()
        blendingDisabled = true
    }

    override fun enableBlending() {
        if (!blendingDisabled) return
        flush()
        blendingDisabled = false
    }

    fun dispose() {
        mesh.destroy()
        if (ownsShader) shader.destroy()
    }

    private fun setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix)
        shader["u_projTrans"] = combinedMatrix
        shader.setUniformi("u_texture", 0)
    }

    protected fun switchTexture(texture: ITexture2D) {
        if (texture !== lastTexture) {
            flush()
            lastTexture = texture
            invTexWidth = 1.0f / texture.width
            invTexHeight = 1.0f / texture.height
        }
    }

    companion object {
        const val DEFAULT_VERTICES_NUM = 1000

        /** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified.  */
        fun createDefaultShader(): Shader {
            val vertexShader = """
attribute vec2 POSITION;
attribute vec4 COLOR;
attribute vec2 UV;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 uv;

void main() {
    v_color = COLOR;
    uv = UV;
    gl_Position =  u_projTrans * vec4(POSITION, 0.0, 1.0);
}"""

            val fragmentShader = """
#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 uv;
uniform sampler2D u_texture;

void main() {
    gl_FragColor = v_color * texture2D(u_texture, uv);
}"""
            return Shader(vertexShader, fragmentShader).apply { depthMask = false }
        }
    }
}
