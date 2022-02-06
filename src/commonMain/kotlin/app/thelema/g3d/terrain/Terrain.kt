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

import app.thelema.ecs.*
import app.thelema.g3d.Blending
import app.thelema.g3d.IMaterial
import app.thelema.g3d.IScene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.gl.IRenderable
import app.thelema.gl.Mesh
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.utils.iterate

class Terrain(
    minTileSize: Float = 10f,
    tileDivisions: Int = 10,
    levelsNum: Int = 10,
    vertexPositionName: String = "POSITION"
): IEntityComponent, UpdatableComponent, IRenderable {
    constructor(block: Terrain.() -> Unit): this() {
        block()
        rebuildComponent()
    }

    override val componentName: String
        get() = "Terrain"

    override var entityOrNull: IEntity? = null

    var minTileSize: Float = minTileSize
        set(value) {
            if (field != value) {
                field = value
                _levels.iterate { it.minTileSize = value }
            }
        }

    var tileDivisions: Int = tileDivisions
        set(value) {
            if (field != value) {
                field = value
                rebuildRequested = true
                _levels = Array(levelsNum) { TerrainLevel(this, it, minTileSize) }
            }
        }

    var levelsNum: Int = levelsNum
        set(value) {
            if (field != value) {
                field = value

            }
        }

    private var rebuildRequested = true

    val plane = TerrainTileMesh().apply {
        getOrCreateEntity()
        divisions = tileDivisions * 2
        padding = 1
        tileSize = 1f
        builder.uvs = false
        builder.tangents = false
        builder.positionName = vertexPositionName
    }

    val frame = TerrainLodFrame2to1Mesh().apply {
        getOrCreateEntity()
        frameSize = 1f
        outerLodDivisions = tileDivisions
        builder.uvs = plane.builder.uvs
        builder.tangents = plane.builder.tangents
        builder.positionName = plane.builder.positionName
        setSideFlags(left = false, right = false, top = false, bottom = false)
    }

    var vertexPositionName: String
        get() = plane.builder.positionName
        set(value) {
            plane.builder.positionName = value
            frame.builder.positionName = value
        }

    private var _levels = Array(levelsNum) { TerrainLevel(this, it, minTileSize) }
    val levels: Array<TerrainLevel>
        get() = _levels

    private var _frameMesh: IMesh = Mesh()
    val frameMesh: IMesh
        get() = _frameMesh

    private var _frameIndexBuffers: Array<IIndexBuffer> = emptyArray()
    val frameIndexBuffers: Array<IIndexBuffer>
        get() = _frameIndexBuffers

    private var _frameIndexBufferMap6x6: Array<Array<IIndexBuffer>> = emptyArray()
    val frameIndexBufferMap6x6: Array<Array<IIndexBuffer>>
        get() = _frameIndexBufferMap6x6

    var maxY = 100f
    var minY = 0f

    var frustum: Frustum? = null

    var useCameraFrustum: Boolean = true

    val listeners: MutableList<TerrainListener> = ArrayList()

    var material: IMaterial? = null
        set(value) {
            field = value
            plane.mesh.material = value
            frame.mesh.material = value
        }

    override var isVisible: Boolean = true

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    override var translucencyPriority: Int
        get() = material?.translucentPriority ?: 0
        set(_) {}

    override val worldPosition: IVec3 = Vec3(0f)

    var tilePositionScaleName: String = "tilePositionScale"

    override fun visibleInFrustum(frustum: Frustum): Boolean = true

    override fun updateComponent(delta: Float) {
        update(ActiveCamera.node.worldPosition)
    }

    fun rebuildComponent() {
        rebuildRequested = false

        plane.divisions = tileDivisions * 2
        frame.outerLodDivisions = tileDivisions

        plane.rebuildComponent()
        frame.rebuildComponent()

        _frameMesh = frame.mesh

        val center = frameMesh.indices!!
        val leftTop = frame.setSideFlags(left = true, right = false, top = true, bottom = false).buildIndices()
        val top = frame.setSideFlags(left = false, right = false, top = true, bottom = false).buildIndices()
        val rightTop = frame.setSideFlags(left = false, right = true, top = true, bottom = false).buildIndices()
        val left = frame.setSideFlags(left = true, right = false, top = false, bottom = false).buildIndices()
        val right = frame.setSideFlags(left = false, right = true, top = false, bottom = false).buildIndices()
        val leftBottom = frame.setSideFlags(left = true, right = false, top = false, bottom = true).buildIndices()
        val bottom = frame.setSideFlags(left = false, right = false, top = false, bottom = true).buildIndices()
        val rightBottom = frame.setSideFlags(left = false, right = true, top = false, bottom = true).buildIndices()

        _frameIndexBuffers = arrayOf(
            leftTop, top, rightTop,
            left, center, right,
            leftBottom, bottom, rightBottom
        )

        _frameIndexBufferMap6x6 = arrayOf(
            arrayOf(leftTop, top, top, top, top, rightTop),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(leftBottom, bottom, bottom, bottom, bottom, rightBottom)
        )
    }

    /** (x, y, z) - camera position */
    fun update(x: Float, y: Float, z: Float) {
        if (useCameraFrustum) {
            frustum = ActiveCamera.frustum
        }

        if (rebuildRequested) rebuildComponent()

        for (i in levels.indices) {
            levels[i].update(x, y, z)
        }
    }

    fun update(cameraPosition: IVec3C) =
        update(cameraPosition.x, cameraPosition.y, cameraPosition.z)

    override fun render(shader: IShader, scene: IScene?) {
        for (i in levels.indices) {
            levels[i].render(shader, scene)
        }
    }

    override fun render(scene: IScene?, shaderChannel: String?) {
        material?.also { material ->
            val shader = if (shaderChannel != null) material.shaderChannels[shaderChannel] else material.shader
            if (shader != null) {
                render(shader, scene)
            }
        }
    }
}
