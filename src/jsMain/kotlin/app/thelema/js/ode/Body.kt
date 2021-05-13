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

package app.thelema.js.ode

external class Body {
    fun isEnabled(): Boolean

    fun setAngularVel(x: Float, y: Float, z: Float): Body
    fun setLinearVel(x: Float, y: Float, z: Float): Body

    fun setMass(mass: Mass): Body

    fun setPosition(x: Float, y: Float, z: Float): Body
    fun setQuaternion(quat: Quaternion): Body
    fun setRotation(rotation: Rotation): Body

    fun getPosition(): FloatArray
    fun getQuaternion(): Quaternion
    fun getRotation(): Rotation

    fun getAngularVel(): FloatArray
    fun getLinearVel(): FloatArray
    fun getMass(): Mass
}