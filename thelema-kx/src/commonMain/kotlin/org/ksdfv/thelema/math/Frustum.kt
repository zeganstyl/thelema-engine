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
class Frustum() {
    constructor(inverseProjectionView: IMat4): this() {
        update(inverseProjectionView)
    }

    /** the six clipping planes, near, far, left, right, top, bottom  */
    val planes = Array(6) { Plane(Vec3(), 0f) }

    /** eight points making up the near and far clipping "rectangles". order is counter clockwise, starting at bottom left */
    val points: List<IVec3> = ArrayList<IVec3>().apply {
        for (i in 0 until 8) {
            add(Vec3())
        }
    }

    /** Updates the clipping plane's based on the given inverse combined projection and view matrix, e.g.
     * @param inverseViewProjection the combined projection and view matrices.
     */
    fun update(inverseViewProjection: IMat4) {
        for (i in clipSpacePlanePoints.indices) {
            val point = points[i]
            point.set(clipSpacePlanePoints[i])
            inverseViewProjection.project(point, point)
        }

        planes[0].set(points[1], points[0], points[2])
        planes[1].set(points[4], points[5], points[7])
        planes[2].set(points[0], points[4], points[3])
        planes[3].set(points[5], points[1], points[6])
        planes[4].set(points[2], points[3], points[6])
        planes[5].set(points[4], points[0], points[1])
    }

    /** Returns whether the point is in the frustum.
     *
     * @param point The point
     * @return Whether the point is in the frustum.
     */
    fun isPointInFrustum(point: IVec3): Boolean {
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
    fun isPointInFrustum(x: Float, y: Float, z: Float): Boolean {
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
    fun isSphereInFrustum(center: Vec3, radius: Float): Boolean {
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
    fun isSphereInFrustum(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 0..5) if (planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given sphere is in the frustum not checking whether it is behind the near and far clipping plane.
     *
     * @param center The center of the sphere
     * @param radius The radius of the sphere
     * @return Whether the sphere is in the frustum
     */
    fun isSphereInFrustumWithoutNearFar(center: IVec3, radius: Float): Boolean {
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
    fun isSphereInFrustumWithoutNearFar(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 2..5) if (planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z < -radius - planes[i].d) return false
        return true
    }

    /** Returns whether the given bounding box is in the frustum.
     * @return Whether the bounding box is in the frustum
     */
    fun boundsInFrustum(center: IVec3, dimensions: IVec3): Boolean {
        return boundsInFrustum(center.x, center.y, center.z, dimensions.x * 0.5f, dimensions.y * 0.5f, dimensions.z * 0.5f)
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
        /** Read only! */
        val clipSpacePlanePoints = arrayOf(
            Vec3(-1f, -1f, -1f), Vec3(1f, -1f, -1f),
            Vec3(1f, 1f, -1f), Vec3(-1f, 1f, -1f),  // near clip
            Vec3(-1f, -1f, 1f), Vec3(1f, -1f, 1f),
            Vec3(1f, 1f, 1f), Vec3(-1f, 1f, 1f) // far clip
        )
    }
}
