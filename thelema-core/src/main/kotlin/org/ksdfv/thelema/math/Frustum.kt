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

package org.ksdfv.thelema.math

/** A truncated rectangular pyramid. Used to define the viewable region and its projection onto the screen. */
class Frustum {
    /** the six clipping planes, near, far, left, right, top, bottom  */
    val planes = Array(6) { Plane(Vec3(), 0f) }

    /** eight points making up the near and far clipping "rectangles". order is counter clockwise, starting at bottom left */
    val nearFarPoints = Array<IVec3>(8) { Vec3() }

    /** Float array of eight points (x,y,z).
     * Points making up the near and far clipping "rectangles".
     * Order is counter clockwise, starting at bottom left */
    val nearFarPointsArray = FloatArray(8 * 3)

    /** Updates the clipping plane's based on the given inverse combined projection and view matrix, e.g.
     * @param inverseProjectionView the combined projection and view matrices.
     */
    fun update(inverseProjectionView: IMat4) {
        System.arraycopy(clipSpacePlanePointsArray, 0, nearFarPointsArray, 0, clipSpacePlanePointsArray.size)
        IMat4.prj(inverseProjectionView.values, nearFarPointsArray, 0, 8, 3)
        var i = 0
        var j = 0
        while (i < 8) {
            val v = nearFarPoints[i]
            v.x = nearFarPointsArray[j++]
            v.y = nearFarPointsArray[j++]
            v.z = nearFarPointsArray[j++]
            i++
        }
        planes[0].set(nearFarPoints[1], nearFarPoints[0], nearFarPoints[2])
        planes[1].set(nearFarPoints[4], nearFarPoints[5], nearFarPoints[7])
        planes[2].set(nearFarPoints[0], nearFarPoints[4], nearFarPoints[3])
        planes[3].set(nearFarPoints[5], nearFarPoints[1], nearFarPoints[6])
        planes[4].set(nearFarPoints[2], nearFarPoints[3], nearFarPoints[6])
        planes[5].set(nearFarPoints[4], nearFarPoints[0], nearFarPoints[1])
    }

    /** Returns whether the point is in the frustum.
     *
     * @param point The point
     * @return Whether the point is in the frustum.
     */
    fun pointInFrustum(point: Vec3): Boolean {
        for (i in planes.indices) {
            val result = planes[i].testPoint(point)
            if (result == Plane.PlaneSide.Back) return false
        }
        return true
    }

    /** Returns whether the point is in the frustum.
     *
     * @param x The X coordinate of the point
     * @param y The Y coordinate of the point
     * @param z The Z coordinate of the point
     * @return Whether the point is in the frustum.
     */
    fun pointInFrustum(x: Float, y: Float, z: Float): Boolean {
        for (i in planes.indices) {
            val result = planes[i].testPoint(x, y, z)
            if (result == Plane.PlaneSide.Back) return false
        }
        return true
    }

    /** Returns whether the given sphere is in the frustum.
     *
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @return Whether the sphere is in the frustum
     */
    fun sphereInFrustum(center: Vec3, radius: Float): Boolean {
        for (i in 0..5) if (planes[i].normal.x * center.x + planes[i].normal.y * center.y + planes[i].normal.z * center.z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given sphere is in the frustum.
     *
     * @param x The X coordinate of the center of the sphere
     * @param y The Y coordinate of the center of the sphere
     * @param z The Z coordinate of the center of the sphere
     * @param radius The radius of the sphere
     * @return Whether the sphere is in the frustum
     */
    fun sphereInFrustum(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 0..5) if (planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given sphere is in the frustum not checking whether it is behind the near and far clipping plane.
     *
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @return Whether the sphere is in the frustum
     */
    fun sphereInFrustumWithoutNearFar(center: Vec3, radius: Float): Boolean {
        for (i in 2..5) if (planes[i].normal.x * center.x + planes[i].normal.y * center.y + planes[i].normal.z * center.z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given sphere is in the frustum not checking whether it is behind the near and far clipping plane.
     *
     * @param x The X coordinate of the center of the sphere
     * @param y The Y coordinate of the center of the sphere
     * @param z The Z coordinate of the center of the sphere
     * @param radius The radius of the sphere
     * @return Whether the sphere is in the frustum
     */
    fun sphereInFrustumWithoutNearFar(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 2..5) if (planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given bounding box is in the frustum.
     * @return Whether the bounding box is in the frustum
     */
    fun boundsInFrustum(center: Vec3, dimensions: Vec3): Boolean {
        return boundsInFrustum(center.x, center.y, center.z, dimensions.x / 2, dimensions.y / 2, dimensions.z / 2)
    }

    /** Returns whether the given bounding box is in the frustum.
     * @return Whether the bounding box is in the frustum
     */
    fun boundsInFrustum(x: Float, y: Float, z: Float, halfWidth: Float, halfHeight: Float, halfDepth: Float): Boolean {
        var i = 0
        val len2 = planes.size
        while (i < len2) {
            if (planes[i].testPoint(x + halfWidth, y + halfHeight, z + halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x + halfWidth, y + halfHeight, z - halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x + halfWidth, y - halfHeight, z + halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x + halfWidth, y - halfHeight, z - halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x - halfWidth, y + halfHeight, z + halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x - halfWidth, y + halfHeight, z - halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x - halfWidth, y - halfHeight, z + halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            if (planes[i].testPoint(x - halfWidth, y - halfHeight, z - halfDepth) != Plane.PlaneSide.Back) {
                i++
                continue
            }
            return false
        }
        return true
    }

    companion object {
        const val CORNER_NXNYNZ = 0
        const val CORNER_NXNYPZ = 1
        const val CORNER_NXPYNZ = 2
        const val CORNER_NXPYPZ = 3
        const val CORNER_PXNYNZ = 4
        const val CORNER_PXNYPZ = 5
        const val CORNER_PXPYNZ = 6
        const val CORNER_PXPYPZ = 7

        val clipSpacePlanePoints = arrayOf(
            Vec3(-1f, -1f, -1f), Vec3(1f, -1f, -1f),
            Vec3(1f, 1f, -1f), Vec3(-1f, 1f, -1f),  // near clip
            Vec3(-1f, -1f, 1f), Vec3(1f, -1f, 1f),
            Vec3(1f, 1f, 1f), Vec3(-1f, 1f, 1f)) // far clip
        val clipSpacePlanePointsArray = FloatArray(8 * 3)

        init {
            var j = 0
            for (v in clipSpacePlanePoints) {
                clipSpacePlanePointsArray[j++] = v.x
                clipSpacePlanePointsArray[j++] = v.y
                clipSpacePlanePointsArray[j++] = v.z
            }
        }
    }
}
