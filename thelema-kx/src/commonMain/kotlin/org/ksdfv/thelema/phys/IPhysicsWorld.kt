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

package org.ksdfv.thelema.phys

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE
import org.ksdfv.thelema.gl.GL_UNSIGNED_INT
import org.ksdfv.thelema.gl.GL_UNSIGNED_SHORT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.mesh.IIndexBufferObject
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.mesh.IVertexBuffer

/** @author zeganstyl */
interface IPhysicsWorld: IOverObject {
    fun setGravity(x: Float, y: Float, z: Float)
    fun getGravity(out: IVec3): IVec3

    fun step(delta: Float)

    /** @param mass if mass is 0, body will be set to static and gravity influence to this body will be disabled */
    fun rigidBody(shape: IShape? = null, mass: Float = 1f): IRigidBody

    fun boxShape(xSize: Float, ySize: Float, zSize: Float): IBoxShape
    fun sphereShape(radius: Float): ISphereShape
    fun capsuleShape(radius: Float, length: Float): ICapsuleShape
    fun cylinderShape(radius: Float, length: Float): ICylinderShape

    fun trimeshShape(vertices: FloatArray, indices: IntArray): ITrimeshShape
    fun trimeshShape(
        vertices: IByteData,
        positionOffset: Int,
        vertexSize: Int,
        indices: IByteData? = null,
        indexType: Int = GL_UNSIGNED_SHORT
    ): ITrimeshShape {
        val verticesArray = FloatArray(vertices.size * 3) { 0f }
        var i = positionOffset
        var floatIndex = 0
        val verticesNum = vertices.size
        while (i < verticesNum) {
            verticesArray[floatIndex] = vertices.readFloat(i)
            verticesArray[floatIndex + 1] = vertices.readFloat(i + 4)
            verticesArray[floatIndex + 2] = vertices.readFloat(i + 8)

            floatIndex += 3
            i += vertexSize
        }

        if (indices != null) {
            return when (indexType) {
                GL_UNSIGNED_BYTE -> {
                    trimeshShape(verticesArray, IntArray(indices.size) { indices.toUInt(it) })
                }

                GL_UNSIGNED_SHORT -> {
                    val buffer = indices.shortView()
                    trimeshShape(verticesArray, IntArray(buffer.size) { buffer.toUInt(it) })
                }

                GL_UNSIGNED_INT -> {
                    val buffer = indices.intView()
                    trimeshShape(verticesArray, IntArray(buffer.size) { buffer.toUInt(it) })
                }

                else -> throw RuntimeException("Index type is unknown: $indexType, it must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT or GL_UNSIGNED_INT")
            }
        } else {
            return trimeshShape(verticesArray, IntArray(vertices.size) { it })
        }
    }

    /** @param positionName name of position attribute in vertex data */
    fun trimeshShape(
        vertices: IVertexBuffer,
        positionName: String = "POSITION",
        indices: IIndexBufferObject? = null
    ): ITrimeshShape {
        val inputs = vertices.vertexInputs
        val pos = inputs[positionName] ?: throw IllegalArgumentException("No vertex attribute with name \"$positionName\"")
        return trimeshShape(vertices.bytes, pos.byteOffset, inputs.bytesPerVertex, indices?.bytes, indices?.type ?: 0)
    }

    fun trimeshShape(mesh: IMesh, positionName: String = "POSITION"): ITrimeshShape {
        return trimeshShape(
            mesh.vertices ?: throw IllegalArgumentException("Mesh must have vertex buffer"),
            positionName,
            mesh.indices
        )
    }

    /** @param positionNames if meshes size is not equal to positionNames size, first position name will be used for all meshes */
    fun trimeshShape(meshes: List<IMesh>, positionNames: List<String> = listOf("POSITION")): ITrimeshShape {
        var indicesNum = 0
        var verticesNum = 0
        for (i in meshes.indices) {
            val mesh = meshes[i]
            val vertices = mesh.vertices ?: throw IllegalArgumentException("Mesh must have vertex buffer")
            verticesNum += vertices.size
            indicesNum += mesh.indices?.size ?: verticesNum
        }

        val verticesArray = FloatArray(verticesNum * 3) { 0f }
        val indicesArray = IntArray(indicesNum) { it }
        var floatIndex = 0
        for (i in meshes.indices) {
            val mesh = meshes[i]
            val vertices = mesh.vertices!!
            val verticesBytes = vertices.bytes

            val posName = if (positionNames.size == meshes.size) positionNames[i] else positionNames[0]

            var iPosition = vertices.vertexInputs.byteOffsetOrNullOf(posName) ?: throw IllegalStateException("No vertex attribute with name \"$posName\"")
            val meshVerticesNum = vertices.size
            val vertexSize = vertices.vertexInputs.bytesPerVertex
            while (iPosition < meshVerticesNum) {
                verticesArray[floatIndex] = verticesBytes.readFloat(iPosition)
                verticesArray[floatIndex + 1] = verticesBytes.readFloat(iPosition + 4)
                verticesArray[floatIndex + 2] = verticesBytes.readFloat(iPosition + 8)
                floatIndex += 3
                iPosition += vertexSize
            }

            var iIndex = 0
            val indices = mesh.indices
            if (indices != null) {
                val meshIndicesNum = indices.size
                when (indices.type) {
                    GL_UNSIGNED_BYTE -> {
                        val buffer = indices.bytes
                        for (j in 0 until meshIndicesNum) {
                            indicesArray[iIndex] = buffer[j].toInt()
                            iIndex++
                        }
                    }

                    GL_UNSIGNED_SHORT -> {
                        val buffer = indices.bytes.shortView()
                        for (j in 0 until meshIndicesNum) {
                            indicesArray[iIndex] = buffer[j].toInt()
                            iIndex++
                        }
                    }

                    GL_UNSIGNED_INT -> {
                        val buffer = indices.bytes.intView()
                        for (j in 0 until meshIndicesNum) {
                            indicesArray[iIndex] = buffer[j]
                            iIndex++
                        }
                    }

                    else -> throw RuntimeException("Index type is unknown: ${indices.type}, it must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT or GL_UNSIGNED_INT")
                }
            } else {
                iIndex += meshVerticesNum
            }
        }
        
        return trimeshShape(verticesArray, indicesArray)
    }

    fun planeShape(): IPlaneShape

    fun rayShape(length: Float): IRay

    fun heightField(
        width: Float,
        depth: Float,
        widthSamples: Int,
        depthSamples: Int,
        scale: Float = 1f,
        offset: Float = 0f,
        thickness: Float = 0f,
        tiling: Boolean = false,
        heightProvider: IHeightProvider? = null
    ): IHeightField

    fun checkCollision(
        shape1: IShape,
        shape2: IShape,
        out: MutableList<IContactInfo> = ArrayList()
    ): MutableList<IContactInfo>

    fun isContactExist(body1: IRigidBody, body2: IRigidBody): Boolean
    fun addPhysicsWorldListener(listener: IPhysicsWorldListener)
    fun removePhysicsWorldListener(listener: IPhysicsWorldListener)

    fun destroy()
}