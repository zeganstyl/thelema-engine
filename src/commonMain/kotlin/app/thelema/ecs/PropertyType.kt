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

package app.thelema.ecs

class PropertyType(override val propertyTypeName: String): IPropertyType {
    override val generics: Array<PropertyType>
        get() = emptyArray()

    companion object {
        val Unknown = PropertyType("Thelema/Unknown")
        val String = PropertyType("Thelema/String")
        val StringEnum = PropertyType("Thelema/StringEnum")
        val IntEnum = PropertyType("Thelema/IntEnum")
        val Bool = PropertyType("Thelema/Bool")
        val Int = PropertyType("Thelema/Int")
        val Float = PropertyType("Thelema/Float")
        val Vec2 = PropertyType("Thelema/Vec2")
        val Vec3 = PropertyType("Thelema/Vec3")
        val Quaternion = PropertyType("Thelema/Quaternion")
        val Vec4 = PropertyType("Thelema/Vec4")
        val Mat3 = PropertyType("Thelema/Mat3")
        val Mat4 = PropertyType("Thelema/Mat4")
        val File = PropertyType("Thelema/File")
        val ShaderNodeInput = PropertyType("Thelema/ShaderNodeInput")
        val ShaderNodeOutput = PropertyType("Thelema/ShaderNodeOutput")
    }
}

class ComponentRefType(var componentName: String): IPropertyType {
    override val generics: Array<PropertyType> = emptyArray()

    override val propertyTypeName: String
        get() = "thelema/Reference"
}