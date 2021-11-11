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

import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.json.IJsonObject
import app.thelema.math.*
import app.thelema.res.RES
import app.thelema.utils.LOG
import kotlin.reflect.KClass

class ComponentDescriptor<T: IEntityComponent>(
    typeName: String,
    val create: () -> T,
    block: ComponentDescriptor<T>.() -> Unit = {}
): ComponentDescriptorList(typeName), IPropertyType {
    val properties = LinkedHashMap<String, IPropertyDescriptor<T, Any?>>()

    override val propertyTypeName: String
        get() = componentName

    init {
        block(this)
    }

    @Suppress("UNCHECKED_CAST")
    fun setAliases(vararg aliases: String) {
        aliases.forEach {
            ECS.allDescriptors[it] = this as ComponentDescriptor<IEntityComponent>
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> setAliases(vararg aliases: KClass<T>) {
        aliases.forEach {
            ECS.allDescriptors[it.simpleName!!] = this as ComponentDescriptor<IEntityComponent>
        }
    }

    fun checkName(name: String) {
        if (name.isEmpty()) throw IllegalStateException("Property descriptor name must not be empty")
        if (properties.containsKey(name)) throw IllegalStateException("Property descriptor \"$name\" already exists")
    }

    fun getProperty(name: String): IPropertyDescriptor<T, Any?> = properties[name]!!

    /** Define property */
    @Suppress("UNCHECKED_CAST")
    fun property(descriptor: IPropertyDescriptor<*, *>): IPropertyDescriptor<T, Any?> {
        checkName(descriptor.name)
        properties[descriptor.name] = descriptor as IPropertyDescriptor<T, Any?>
        return descriptor
    }

    /** Define boolean property */
    fun bool(name: String, get: T.() -> Boolean, set: T.(value: Boolean) -> Unit) = property(object : IPropertyDescriptor<T, Boolean> {
        override val name: String = name
        override val type = PropertyType.Bool
        override fun setValue(component: T, value: Boolean) = set(component, value)
        override fun getValue(component: T): Boolean = get(component)
        override fun default(): Boolean = false
        override fun readJson(component: T, json: IJsonObject) = set(component, json.bool(name, default()))
        override fun writeJson(component: T, json: IJsonObject) { json[name] = component.get() }
    })

    /** Define integer property */
    fun int(name: String, get: T.() -> Int, set: T.(value: Int) -> Unit) = property(object : IPropertyDescriptor<T, Int> {
        override val name: String = name
        override val type = PropertyType.Int
        override fun setValue(component: T, value: Int) = set(component, value)
        override fun getValue(component: T): Int = get(component)
        override fun default(): Int = 0
        override fun readJson(component: T, json: IJsonObject) = set(component, json.int(name, default()))
        override fun writeJson(component: T, json: IJsonObject) { json[name] = component.get() }
    })

    /** Define float property */
    fun float(name: String, get: T.() -> Float, set: T.(value: Float) -> Unit) = property(object : IPropertyDescriptor<T, Float> {
        override val name: String = name
        override val type = PropertyType.Float
        override fun setValue(component: T, value: Float) = set(component, value)
        override fun getValue(component: T): Float = get(component)
        override fun default(): Float = 0f
        override fun readJson(component: T, json: IJsonObject) = set(component, json.float(name, default()))
        override fun writeJson(component: T, json: IJsonObject) { json[name] = component.get() }
    })

    /** Define 2-dimensional float vector property */
    fun vec2(name: String, get: T.() -> IVec2, set: T.(value: IVec2) -> Unit) = property(object : IPropertyDescriptor<T, IVec2> {
        override val name: String = name
        override val type = PropertyType.Vec2
        override fun setValue(component: T, value: IVec2) = set(component, value)
        override fun getValue(component: T): IVec2 = get(component)
        override fun default(): IVec2 = MATH.Zero2
        override fun readJson(component: T, json: IJsonObject) {
            json.array(name) { set(component, Vec2(float(0, 0f), float(1, 0f))) }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            get(component).also {
                if (it.x != 0f || it.y != 0f) {
                    json.setArray(name) { add(it.x, it.y) }
                }
            }
        }
    })

    /** Define 3-dimensional float vector property */
    fun vec3(name: String, get: T.() -> IVec3, set: T.(value: IVec3) -> Unit) = property(object : IPropertyDescriptor<T, IVec3> {
        override val name: String = name
        override val type = PropertyType.Vec3
        override fun setValue(component: T, value: IVec3) = set(component, value)
        override fun getValue(component: T): IVec3 = get(component)
        override fun default(): IVec3 = MATH.Zero3
        override fun readJson(component: T, json: IJsonObject) {
            json.array(name) { set(component, Vec3(float(0, 0f), float(1, 0f), float(2, 0f))) }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            get(component).also {
                if (it.x != 0f || it.y != 0f || it.z != 0f) {
                    json.setArray(name) { add(it.x, it.y, it.z) }
                }
            }
        }
    })

    fun vec3(name: String, default: IVec3, get: T.() -> IVec3, set: T.(value: IVec3) -> Unit) = property(object : IPropertyDescriptor<T, IVec3> {
        override val name: String = name
        override val type = PropertyType.Vec3
        override fun setValue(component: T, value: IVec3) = set(component, value)
        override fun getValue(component: T): IVec3 = get(component)
        override fun default(): IVec3 = default
        override fun readJson(component: T, json: IJsonObject) {
            json.array(name) { set(component, Vec3(float(0, default.x), float(1, default.y), float(2, default.z))) }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            get(component).also {
                if (it.x != default.x || it.y != default.y || it.z != default.z) {
                    json.setArray(name) { add(it.x, it.y, it.z) }
                }
            }
        }
    })

    /** Define 4-dimensional float vector property */
    fun vec4(name: String, get: T.() -> IVec4, set: T.(value: IVec4) -> Unit) = property(object : IPropertyDescriptor<T, IVec4> {
        override val name: String = name
        override val type = PropertyType.Vec4
        override fun setValue(component: T, value: IVec4) = set(component, value)
        override fun getValue(component: T): IVec4 = get(component)
        override fun default(): IVec4 = MATH.Zero3One1
        override fun readJson(component: T, json: IJsonObject) {
            json.array(name) { set(component, Vec4(float(0, 0f), float(1, 0f), float(2, 0f), float(3, 1f))) }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            get(component).also {
                if (it.x != 0f || it.y != 0f || it.z != 0f || it.w != 1f) {
                    json.setArray(name) { add(it.x, it.y, it.z, it.w) }
                }
            }
        }
    })

    /** Define 4x4 matrix property */
    fun mat4(name: String, get: T.() -> IMat4, set: T.(value: IMat4) -> Unit) = property(object : IPropertyDescriptor<T, IMat4> {
        override val name: String = name
        override val type = PropertyType.Mat4
        override val useJsonReadWrite: Boolean = false
        override fun setValue(component: T, value: IMat4) = set(component, value)
        override fun getValue(component: T): IMat4 = get(component)
        override fun default(): IMat4 = MATH.IdentityMat4
        override fun readJson(component: T, json: IJsonObject) {}
        override fun writeJson(component: T, json: IJsonObject) {}
    })

    /** Define string-property */
    fun string(name: String, get: T.() -> String, set: T.(value: String) -> Unit) = property(object : IPropertyDescriptor<T, String> {
        override val name: String = name
        override val type = PropertyType.String
        override fun setValue(component: T, value: String) = set(component, value)
        override fun getValue(component: T): String = get(component)
        override fun default(): String = ""
        override fun readJson(component: T, json: IJsonObject) = set(component, json.string(name, default()))
        override fun writeJson(component: T, json: IJsonObject) { json[name] = component.get() }
    })

    fun stringEnum(name: String, values: List<String>, get: T.() -> String, set: T.(value: String) -> Unit) =
        property(StringEnumPropertyDesc(name, values, get, set))

    /** Define File-property (string) */
    fun file(name: String, get: T.() -> IFile?, set: T.(value: IFile?) -> Unit) = property(object : IPropertyDescriptor<T, IFile?> {
        override val name: String = name
        override val type = PropertyType.File
        override fun setValue(component: T, value: IFile?) = set(component, value)
        override fun getValue(component: T): IFile? = get(component)
        override fun default(): IFile? = null
        override fun readJson(component: T, json: IJsonObject) {
            set(component, FS.file(json.string(name, ""), json.string("${name}Location", FileLocation.Project)))
        }
        override fun writeJson(component: T, json: IJsonObject) {
            component.get()?.also { file ->
                json[name] = file.path
                if (file.location != FileLocation.Project) json["${name}Location"] = file.location
            }
        }
    })

    /** Define relative reference-property to component
     * @param name property name
     * @param requiredComponent component name
     * @param get get component reference from this component's property
     * @param set set component reference to this component's property */
    fun <V: IEntityComponent> ref(name: String, requiredComponent: String, get: T.() -> V?, set: T.(value: V?) -> Unit) = property(object : IPropertyDescriptor<T, V?> {
        override val name: String = name
        override val type = ComponentRefType(requiredComponent)
        override fun setValue(component: T, value: V?) = set(component, value)
        override fun getValue(component: T): V? = get(component)
        override fun default(): V? = null
        @Suppress("UNCHECKED_CAST")
        override fun copy(component: T, other: T) {
            val otherRef = get(other) as IEntityComponent?
            if (otherRef != null) {
                val path = other.entity.getRelativePathTo(otherRef.entity)
                val ref = component.entity.getEntityByPath(path)?.componentOrNull(requiredComponent)
                if (ref != null) {
                    set(component, ref as V)
                } else {
                    LOG.error("${component.path}: can't link component reference $name to path: $path")
                }
            } else {
                set(component, null)
            }
        }
        @Suppress("UNCHECKED_CAST")
        override fun readJson(component: T, json: IJsonObject) {
            val path = json.string(name, "")
            val entity = component.entityOrNull
            if (path.isNotEmpty() && entity != null) {
                set(component, entity.getRootEntity().makePath(path).componentTyped<V>(requiredComponent))
            }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            val path = get(component)?.entityOrNull?.path ?: ""
            if (path.isNotEmpty()) json[name] = path
        }
    })

    /** Define absolute reference-property to component
     * @param name property name
     * @param requiredComponent component name
     * @param get get component reference from this component's property
     * @param set set component reference to this component's property */
    @Suppress("UNCHECKED_CAST")
    fun <V: IEntityComponent> refAbs(name: String, requiredComponent: String, get: T.() -> V?, set: T.(value: V?) -> Unit) = property(object : IPropertyDescriptor<T, V?> {
        override val name: String = name
        override val type = ComponentRefType(requiredComponent)
        override fun setValue(component: T, value: V?) = set(component, value)
        override fun getValue(component: T): V? = get(component)
        override fun default(): V? = null
        override fun readJson(component: T, json: IJsonObject) {
            val path = json.string(name, "")
            val entity = component.entityOrNull
            if (path.isNotEmpty() && entity != null) {
                set(component, RES.entity.makePath(path).componentTyped<V>(requiredComponent))
            }
        }
        override fun writeJson(component: T, json: IJsonObject) {
            val path = (get(component) as IEntityComponent?)?.entityOrNull?.path ?: ""
            if (path.isNotEmpty()) json[name] = path
        }
    })

    /** Define reference-property to component
     * @param name property name
     * @param get get component reference from this component's property
     * @param set set component reference to this component's property */
    inline fun <reified V: IEntityComponent> ref(name: String, noinline get: T.() -> V?, noinline set: T.(value: V?) -> Unit) {
        ref(name, V::class.simpleName!!, get, set)
    }

    inline fun <reified V: IEntityComponent> refAbs(name: String, noinline get: T.() -> V?, noinline set: T.(value: V?) -> Unit) {
        refAbs(name, V::class.simpleName!!, get, set)
    }
}