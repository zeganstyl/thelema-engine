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

/** Encapsulates a ray having a starting position and a unit length direction.
 *
 * @author badlogicgames@gmail.com
 */
class Ray(val origin: Vec3 = Vec3(), val direction: Vec3 = Vec3()) {
    init {
        direction.nor()
    }

    /** @return a copy of this ray. */
    fun cpy() = Ray(origin, direction)

    /** Returns the endpoint given the distance. This is calculated as startpoint + distance * direction.
     * @param out The vector to set to the result
     * @param distance The distance from the end point to the start point.
     * @return The out param
     */
    fun getEndPoint(out: IVec3, distance: Float): IVec3 {
        return out.set(direction).scl(distance).add(origin)
    }

    /** Multiplies the ray by the given matrix. Use this to transform a ray into another coordinate system.
     *
     * @param mat The matrix
     * @return This ray for chaining.
     */
    fun mul(mat: IMat4): Ray {
        val ox = origin.x
        val oy = origin.y
        val oz = origin.z

        origin.add(direction)
        origin.mul(mat)
        direction.set(origin)

        origin.set(ox, oy, oz)
        origin.mul(mat)

        direction.sub(origin)

        return this
    }

    /** {@inheritDoc}  */
    override fun toString() = "ray [$origin:$direction]"

    /** Sets the starting position and the direction of this ray.
     *
     * @param origin The starting position
     * @param direction The direction
     * @return this ray for chaining
     */
    fun set(origin: Vec3, direction: Vec3): Ray {
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    /** Sets this ray from the given starting position and direction.
     *
     * @param x The x-component of the starting position
     * @param y The y-component of the starting position
     * @param z The z-component of the starting position
     * @param dx The x-component of the direction
     * @param dy The y-component of the direction
     * @param dz The z-component of the direction
     * @return this ray for chaining
     */
    fun set(x: Float, y: Float, z: Float, dx: Float, dy: Float, dz: Float): Ray {
        origin.set(x, y, z)
        direction.set(dx, dy, dz)
        return this
    }

    /** Sets the starting position and direction from the given ray
     *
     * @param ray The ray
     * @return This ray for chaining
     */
    fun set(ray: Ray): Ray {
        origin.set(ray.origin)
        direction.set(ray.direction)
        return this
    }
}
