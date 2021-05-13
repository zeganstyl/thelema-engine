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

import app.thelema.math.*
import app.thelema.res.RES
import app.thelema.utils.LOG
import kotlin.reflect.KClass

class ComponentDescriptor<T: IEntityComponent>(
    typeName: String,
    val create: () -> T,
    block: ComponentDescriptor<T>.() -> Unit = {}
): ComponentDescriptorList(typeName), IPropertyType {
    val properties = LinkedHashMap<String, PropertyDescriptor<T, Any?>>()

    override val propertyTypeName: String
        get() = componentName

    init {
        block(this)
    }

    fun addAliases(vararg aliases: String) {
        aliases.forEach {
            ECS.allDescriptors[it] = this as ComponentDescriptor<IEntityComponent>
        }
    }

    fun <T: Any> addAliases(vararg aliases: KClass<T>) {
        aliases.forEach {
            ECS.allDescriptors[it.simpleName!!] = this as ComponentDescriptor<IEntityComponent>
        }
    }

    private fun checkName(name: String) {
        if (name.isEmpty()) throw IllegalStateException("Property descriptor name must not be empty")
        if (properties.containsKey(name)) throw IllegalStateException("Property descriptor $name already exists")
    }

    fun getProperty(name: String): PropertyDescriptor<T, Any?> = properties[name]!!

    /** Define property */
    fun property(descriptor: PropertyDescriptor<*, *>) {
        properties[descriptor.name] = descriptor as PropertyDescriptor<T, Any?>
    }

    /** Define property */
    fun <V: Any?> property(name: String, type: String, get: T.() -> V, set: T.(value: V) -> Unit): PropertyDescriptor<T, V> {
        checkName(name)
        val propertyDescriptor = PropertyDescriptor<T, V>(name)
        propertyDescriptor.type = type
        propertyDescriptor.set = set
        propertyDescriptor.get = get
        property(propertyDescriptor)
        return propertyDescriptor
    }

    /** Define boolean property */
    fun bool(name: String, get: T.() -> Boolean, set: T.(value: Boolean) -> Unit): PropertyDescriptor<T, Boolean> {
        val property = property(name, PropertyType.Bool.propertyTypeName, get, set)
        property.default = { false }
        property.readJson = { set(it.bool(name, property.default())) }
        property.writeJson = { it[name] = get() }
        return property
    }

    /** Define integer property */
    fun int(name: String, get: T.() -> Int, set: T.(value: Int) -> Unit): PropertyDescriptor<T, Int> {
        val property = property(name, PropertyType.Int.propertyTypeName, get, set)
        property.default = { 0 }
        property.readJson = { set(it.int(name, property.default())) }
        property.writeJson = { it[name] = get() }
        return property
    }

    /** Define float property */
    fun float(name: String, get: T.() -> Float, set: T.(value: Float) -> Unit): PropertyDescriptor<T, Float> {
        val property = property(name, PropertyType.Float.propertyTypeName, get, set)
        property.default = { 0f }
        property.readJson = { set(it.float(name, property.default())) }
        property.writeJson = { it[name] = get() }
        return property
    }

    /** Define 2-dimensional float vector property */
    fun vec2(name: String, get: T.() -> IVec2, set: T.(value: IVec2) -> Unit): PropertyDescriptor<T, IVec2>  {
        val property = property(name, PropertyType.Vec2.propertyTypeName, get, set)
        property.default = { MATH.Zero2 }
        property.readJson = {
            it.array(name) {
                val default = property.default()
                set(Vec2(float(0, default.x), float(1, default.y)))
            }
        }
        property.writeJson = {
            it.setArray(name) {
                val vec = get()
                add(vec.x, vec.y)
            }
        }
        return property
    }

    /** Define 3-dimensional float vector property */
    fun vec3(name: String, get: T.() -> IVec3, set: T.(value: IVec3) -> Unit): PropertyDescriptor<T, IVec3> {
        val property = property(name, PropertyType.Vec3.propertyTypeName, get, set)
        property.default = { MATH.Zero3 }
        property.readJson = {
            it.array(name) {
                val default = property.default()
                set(Vec3(float(0, default.x), float(1, default.y), float(2, default.z)))
            }
        }
        property.writeJson = {
            it.setArray(name) {
                val vec = get()
                add(vec.x, vec.y, vec.z)
            }
        }
        return property
    }

    /** Define 4-dimensional float vector property */
    fun vec4(name: String, get: T.() -> IVec4, set: T.(value: IVec4) -> Unit): PropertyDescriptor<T, IVec4> {
        val property = property(name, PropertyType.Vec4.propertyTypeName, get, set)
        property.default = { MATH.Zero3One1 }
        property.readJson = {
            it.array(name) {
                val default = property.default()
                set(Vec4(float(0, default.x), float(1, default.y), float(2, default.z), float(3, default.w)))
            }
        }
        property.writeJson = {
            it.setArray(name) {
                val vec = get()
                add(vec.x, vec.y, vec.z, vec.w)
            }
        }
        return property
    }

    /** Define 4x4 matrix property */
    fun mat4(name: String, get: T.() -> IMat4, set: T.(value: IMat4) -> Unit) =
        property(name, PropertyType.Mat4.propertyTypeName, get, set)

    /** Define string-property */
    fun string(name: String, get: T.() -> String, set: T.(value: String) -> Unit): PropertyDescriptor<T, String> {
        val property = property(name, PropertyType.String.propertyTypeName, get, set)
        property.default = { "" }
        property.readJson = { set(it.string(name, property.default())) }
        property.writeJson = { it[name] = get() }
        return property
    }

    /** Define URI-property (string) */
    fun uri(name: String, get: T.() -> String, set: T.(value: String) -> Unit): PropertyDescriptor<T, String> {
        val property = property(name, PropertyType.URI.propertyTypeName, get, set)
        property.default = { "" }
        property.readJson = { set(it.string(name, property.default())) }
        property.writeJson = { it[name] = get() }
        return property
    }

    /** Define reference-property to component
     * @param name property name
     * @param requiredComponent component name
     * @param get get component reference from this component's property
     * @param set set component reference to this component's property */
    fun <V: Any?> ref(name: String, requiredComponent: String, get: T.() -> V, set: T.(value: V) -> Unit) {
        checkName(name)
        val descriptor = PropertyDescriptor<T, V>(name)
        descriptor.type = requiredComponent
        descriptor.set = set
        descriptor.copy = { other ->
            val otherRef = get(other) as IEntityComponent?
            if (otherRef != null) {
                val path = other.entity.getRelativePathTo(otherRef.entity)
                val component = entity.getEntityByPath(path)?.getComponentOrNull(requiredComponent)
                if (component != null) {
                    descriptor.set(this, component as V)
                } else {
                    LOG.error("${this.path}: can't link component reference $name to path: $path")
                }
            } else {
                set(this, null as V)
            }
        }
        descriptor.get = get
        descriptor.readJson = { json ->
            val path = json.string(name, "")
            if (path.isNotEmpty()) {
                RES.onComponentAdded(json.string(name, ""), requiredComponent) { component ->
                    descriptor.set.invoke(this, component as V)
                }
            }
        }
        descriptor.writeJson = { json ->
            val path = (get() as IEntityComponent?)?.entity?.path ?: ""
            if (path.isNotEmpty()) json[name] = path
        }
        property(descriptor)
    }

    /** Define reference-property to component
     * @param name property name
     * @param get get component reference from this component's property
     * @param set set component reference to this component's property */
    inline fun <reified V: Any?> ref(name: String, noinline get: T.() -> V?, noinline set: T.(value: V?) -> Unit) {
        ref(name, V::class.simpleName!!, get, set)
    }
}