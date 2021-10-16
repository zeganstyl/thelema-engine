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


/** A plane defined via a unit length normal and the distance from the origin.
 *
 * @author badlogicgames@gmail.com, zeganstyl
 */
class Plane(
    val normal: IVec3 = Vec3(0f, 1f, 0f),
    /** The distance to the origin */
    var d: Float = 0f
) {
    /** Constructs a new plane based on the normal and a point on the plane. */
    constructor(normal: IVec3, point: IVec3): this() {
        this.normal.set(normal).nor()
        d = -this.normal.dot(point)
    }

    /** Constructs a new plane out of the three given points that are considered to be on the plane.
     * The normal is calculated via a cross product between (point1-point2)x(point2-point3) */
    constructor(point1: IVec3, point2: IVec3, point3: IVec3): this() {
        set(point1, point2, point3)
    }

    /** Sets the plane normal and distance to the origin based on the three given points which are considered to be on the plane.
     * The normal is calculated via a cross product between (point1-point2)x(point2-point3) */
    fun set(point1: IVec3, point2: IVec3, point3: IVec3) {
        normal.set(point1).sub(point2).crs(point2.x - point3.x, point2.y - point3.y, point2.z - point3.z).nor()
        d = -point1.dot(normal)
    }

    /** Sets the plane normal (nx, ny, nz) and distance (d) */
    fun set(nx: Float, ny: Float, nz: Float, d: Float) {
        normal.set(nx, ny, nz)
        this.d = d
    }

    /** Calculates the shortest signed distance between the plane and the given point.
     *
     * If distance > 0, point is in the front of plane.
     *
     * If distance < 0, point is in the back of plane.
     *
     * If distance = 0, point is on the plane.
     *
     * @return the shortest signed distance between the plane and the point */
    fun distance(point: IVec3): Float = normal.dot(point) + d

    /** Calculates the shortest signed distance between the plane and the given point.
     *
     * If distance > 0, point is in the front of plane.
     *
     * If distance < 0, point is in the back of plane.
     *
     * If distance = 0, point is on the plane.
     *
     * @return the shortest signed distance between the plane and the point */
    fun distance(x: Float, y: Float, z: Float): Float = normal.dot(x, y, z) + d

    /** Sets the plane to the given point and normal. */
    fun set(point: IVec3, normal: IVec3) {
        this.normal.set(normal)
        d = point.dot(normal)
    }

    fun set(pointX: Float, pointY: Float, pointZ: Float, norX: Float, norY: Float, norZ: Float) {
        normal.set(norX, norY, norZ)
        d = -(pointX * norX + pointY * norY + pointZ * norZ)
    }

    /** Sets this plane from the given plane */
    fun set(plane: Plane) {
        normal.set(plane.normal)
        d = plane.d
    }

    fun getIntersectionPoint(rayDirection: IVec3, rayOrigin: IVec3, out: IVec3): IVec3 {
        val diffX = rayOrigin.x - normal.x * d
        val diffY = rayOrigin.y - normal.y * d
        val diffZ = rayOrigin.z - normal.z * d
        val prod3 = MATH.dot(diffX, diffY, diffZ, normal.x, normal.y, normal.z) / MATH.dot(rayDirection.x, rayDirection.y, rayDirection.z, normal.x, normal.y, normal.z)
        return out.set(rayOrigin).sub(rayDirection.x * prod3, rayDirection.y * prod3, rayDirection.z * prod3)
    }

    fun getIntersectionPoint(ray: Ray, out: IVec3): IVec3 = getIntersectionPoint(ray.direction, ray.origin, out)

    override fun toString(): String = "$normal, $d"
}
