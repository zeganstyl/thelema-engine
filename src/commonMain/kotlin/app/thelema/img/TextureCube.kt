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

package app.thelema.img

import app.thelema.ecs.IEntity
import app.thelema.gl.*
import app.thelema.math.IRectangle
import app.thelema.math.Rectangle
import app.thelema.res.RES
import app.thelema.res.load

/**
 * @author zeganstyl */
class TextureCube(): Texture(GL_TEXTURE_CUBE_MAP) {
    constructor(block: TextureCube.() -> Unit): this() {
        initTexture()
        block(this)
        GL.glBindTexture(glTarget, 0)
    }

    constructor(
        px: String,
        nx: String,
        py: String,
        ny: String,
        pz: String,
        nz: String,
        mipLevel: Int = 0,
        error: (status: Int) -> Unit = {},
        ready: TextureCube.() -> Unit = {}
    ): this() {
        load(px, nx, py, ny, pz, nz, mipLevel, error, ready)
    }

    constructor(uri: String, layout: Array<IRectangle>, mipLevel: Int = 0): this() {
        load(uri, layout, mipLevel)
    }

    constructor(image: IImage, layout: Array<IRectangle>, mipLevel: Int = 0): this() {
        load(image, layout, mipLevel)
    }

    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "TextureCube"

    override var width: Int
        get() = px.width
        set(_) {}

    override var height: Int
        get() = px.height
        set(_) {}

    override val depth: Int
        get() = 0

    val sides: List<ITexture2D> = listOf(
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_POSITIVE_X),
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
        TextureCubeSide(this, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z)
    )

    val px: ITexture2D
        get() = sides[0]
    val nx: ITexture2D
        get() = sides[1]
    val py: ITexture2D
        get() = sides[2]
    val ny: ITexture2D
        get() = sides[3]
    val pz: ITexture2D
        get() = sides[4]
    val nz: ITexture2D
        get() = sides[5]

    override fun initTexture(color: Int) {
        for (i in 0 until 6) {
            sides[i].initTexture(color)
        }

        minFilter = GL_LINEAR
        magFilter = GL_LINEAR
        sWrap = GL_CLAMP_TO_EDGE
        tWrap = GL_CLAMP_TO_EDGE
        rWrap = GL_CLAMP_TO_EDGE
    }

    fun setupAsRenderTarget(resolution: Int, pixelFormat: Int, internalFormat: Int, pixelChannelType: Int, mipLevel: Int) {
        sides.forEach { it.load(resolution, resolution, null, internalFormat, pixelFormat, pixelChannelType, mipLevel) }
        minFilter = GL_NEAREST
        magFilter = GL_LINEAR
        sWrap = GL_CLAMP_TO_EDGE
        tWrap = GL_CLAMP_TO_EDGE
        rWrap = GL_CLAMP_TO_EDGE
    }

    fun setupAsRenderTarget(resolution: Int) =
        setupAsRenderTarget(resolution, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, 0)

    /**
     * @param px positive X
     * @param nx negative X
     * @param py positive Y
     * @param ny negative Y
     * @param pz positive Z
     * @param nz negative Z
     *  */
    fun load(
        px: String,
        nx: String,
        py: String,
        ny: String,
        pz: String,
        nz: String,
        mipLevel: Int = 0,
        error: (status: Int) -> Unit = {},
        ready: TextureCube.() -> Unit = {}
    ) {
        initTexture()
        var counter = 0
        val checkComplete: ITexture2D.() -> Unit = {
            counter++
            if (counter == 6) {
                ready(this@TextureCube)
                this@TextureCube.unbind()
            }
        }
        this.px.image = RES.load(px)
        //this.px.load(px, mipLevel, error, checkComplete)
        this.nx.load(nx, mipLevel, error, checkComplete)
        this.py.load(py, mipLevel, error, checkComplete)
        this.ny.load(ny, mipLevel, error, checkComplete)
        this.pz.load(pz, mipLevel, error, checkComplete)
        this.nz.load(nz, mipLevel, error, checkComplete)
    }

    /** Helps to load sides from single image.
     * For example skybox sides may be stored in several sections with some layout in single image.
     * @param layout array size must be 6, sides order in array: px, nx, py, ny, pz, nz */
    fun load(image: IImage, layout: Array<IRectangle>, mipLevel: Int = 0) {
        initTexture()

        for (i in layout.indices) {
            val rect = layout[i]

            val subImage = image.subImage(
                (rect.x * image.width).toInt(),
                (rect.y * image.height).toInt(),
                (rect.width * image.width).toInt(),
                (rect.height * image.height).toInt()
            )

            sides[i].load(subImage, mipLevel)

            subImage.destroy()
        }
    }

    /** Helps to load sides from single image.
     * For example skybox sides may be stored in several sections with some layout in single image.
     * @param layout array size must be 6, sides order in array: px, nx, py, ny, pz, nz */
    fun load(uri: String, layout: Array<IRectangle>, mipLevel: Int = 0) {
        initTexture()
        RES.load<IImage>(uri) {
            onLoaded {
                load(this, layout, mipLevel)
            }
        }
    }

    companion object {
        /**
         * | 00 py 00 00 |
         *
         * | nx pz px nz |
         *
         * | 00 ny 00 00 |
         * */
        fun defaultSkyboxLayout(): Array<IRectangle> {
            val h = 1f / 3f
            val w = 0.25f
            return arrayOf(
                Rectangle(w * 2f, h, w, h),
                Rectangle(0f, h, w, h),
                Rectangle(w, 0f, w, h),
                Rectangle(w, h * 2f, w, h),
                Rectangle(w, h, w, h),
                Rectangle(w * 3f, h, w, h)
            )
        }
    }
}
