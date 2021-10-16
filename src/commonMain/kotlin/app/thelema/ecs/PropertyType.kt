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
    override val generics: Array<PropertyType> = emptyArray()

    companion object {
        val Unknown = PropertyType("thelema/Unknown")
        val String = PropertyType("thelema/String")
        val StringEnum = PropertyType("thelema/StringEnum")
        val Bool = PropertyType("thelema/Bool")
        val Int = PropertyType("thelema/Int")
        val Float = PropertyType("thelema/Float")
        val Vec2 = PropertyType("thelema/Vec2")
        val Vec3 = PropertyType("thelema/Vec3")
        val Vec4 = PropertyType("thelema/Vec4")
        val Mat3 = PropertyType("thelema/Mat3")
        val Mat4 = PropertyType("thelema/Mat4")
        val File = PropertyType("thelema/File")
    }
}
