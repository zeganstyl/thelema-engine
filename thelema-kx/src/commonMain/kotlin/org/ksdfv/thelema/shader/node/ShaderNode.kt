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

package org.ksdfv.thelema.shader.node

import org.ksdfv.thelema.g3d.IObject3D
import org.ksdfv.thelema.g3d.IScene
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.mesh.IMesh
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
abstract class ShaderNode: IShaderNode {
    protected val inputInternal = LinkedHashMap<String, IShaderData>()
    protected val outputInternal = LinkedHashMap<String, IShaderData>()

    override var shaderOrNull: IShader? = null

    override val input: Map<String, IShaderData>
        get() = inputInternal

    override val output: Map<String, IShaderData>
        get() = outputInternal

    val attribute: String
        get() = if (shader.version >= 130) "in" else "attribute"

    val varOut: String
        get() = if (shader.version >= 130) "out" else "varying"

    val varIn: String
        get() = if (shader.version >= 130) "in" else "varying"

    protected fun defOut(initial: IShaderData): IShaderData {
        outputInternal[initial.name] = initial
        return initial
    }

    override fun setInput(name: String, value: IShaderData?) {
        val input = inputInternal[name]
        if (input != null) {
            val toRemove = input.connectedTo.filter {
                it.node == this && it.inputName == name
            }
            for (i in toRemove.indices) {
                input.connectedTo.remove(toRemove[i])
            }
        }

        if (value != null) {
            if (value.connectedTo.firstOrNull { it.node == this && it.inputName == name } == null) {
                inputInternal[name] = value
                value.connectedTo.add(ShaderNodeLink(this, name))
            }
        } else {
            inputInternal.remove(name)
        }
    }

    override fun prepareToBuild() {
        output.values.forEach { it.container = this }
    }

    override fun shaderCompiled() {}
    override fun prepareToDrawScene(scene: IScene) {}
    override fun prepareObjectData(obj: IObject3D) {}
    override fun prepareToDrawMesh(mesh: IMesh) {}

    override fun executionFrag(out: StringBuilder) = Unit
    override fun executionVert(out: StringBuilder) = Unit
    override fun declarationVert(out: StringBuilder) = Unit
    override fun declarationFrag(out: StringBuilder) = Unit

    override fun set(other: IShaderNode): IShaderNode {
        return this
    }

    override fun read(json: IJsonObject) {}

    override fun write(json: IJsonObject) {}
}