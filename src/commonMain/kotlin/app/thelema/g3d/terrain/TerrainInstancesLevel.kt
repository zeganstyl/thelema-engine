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

import app.thelema.data.DATA
import app.thelema.data.IFloatData
import app.thelema.gl.IVertexBuffer
import app.thelema.img.ImageSampler
import app.thelema.math.Perlin
import app.thelema.gl.VertexBuffer

class TerrainInstancesLevel(var imageSampler: ImageSampler, block: IVertexBuffer.() -> Unit) {
    val buffers: Array<Array<IVertexBuffer>> = Array(3) {
        Array(3) {
            val vertices = VertexBuffer()
            block(vertices)
            vertices.initVertexBuffer(1)
            vertices
        }
    }

    val bufferFloatViews: Array<Array<IFloatData>> = Array(3) { i ->
        Array(3) { j -> buffers[i][j].bytes.floatView() }
    }

    val renderFlags: Array<Array<Boolean>> = Array(3) { Array(3) { false } }

    var noise: Perlin? = null

    /** if function returns true, instance will be added */
    var putInstance: (buffer: IFloatData, x: Float, y: Float, r: Int, g: Int, b: Int, a: Int) -> Boolean = { buffer, x, y, r, g, b, a ->
        if (g > 128) {
            if ((noise?.sample(x, 0.7f, y) ?: 0f) > 0.2f || g > 150) {
                buffer.put(x + (noise?.sample(x, 0.3f, y) ?: 0f))
                buffer.put(0f)
                buffer.put(y + (noise?.sample(x, 1.7f, y) ?: 0f))
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    var renderInstances: (instances: IVertexBuffer, i: Int, j: Int) -> Unit = { _, _, _ -> }

    fun clearRenderFlags() {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                renderFlags[i][j] = false
            }
        }
    }

    fun render() {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (renderFlags[i][j]) renderInstances(buffers[i][j], i, j)
            }
        }
    }

    fun rebuild(startX: Float, startZ: Float, tileSize: Float) {
        var tileX = startX
        for (i in 0 until 3) {
            var tileZ = startZ
            for (j in 0 until 3) {
                val buffer = buffers[i][j]
                val instanceSize = buffer.bytesPerVertex / 4

                var instancesNum = 0

                var floatView = bufferFloatViews[i][j]
                floatView.rewind()
                imageSampler.iteratePixels(tileX, tileZ, tileSize, tileSize) { x, y, r, g, b, a ->
                    if (putInstance(floatView, x, y, r, g, b, a)) instancesNum++

                    // expand buffer
                    if (instancesNum * instanceSize == floatView.limit) {
                        val oldBytes = buffer.bytes
                        val newBytes = DATA.bytes(buffer.bytes.limit * 2)
                        floatView = newBytes.floatView()
                        for (k in 0 until oldBytes.limit) {
                            newBytes[k] = oldBytes[k]
                        }
                        DATA.destroyBytes(oldBytes)
                        buffer.bytes = newBytes
                        bufferFloatViews[i][j] = floatView
                    }
                }
                floatView.rewind()

                buffer.uploadBufferToGpu()
                //buffer.instancesToRender = instancesNum

                tileZ += tileSize
            }
            tileX += tileSize
        }
    }
}
