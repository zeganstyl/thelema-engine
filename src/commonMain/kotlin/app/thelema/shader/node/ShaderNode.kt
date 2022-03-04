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

package app.thelema.shader.node

import app.thelema.ecs.IEntity
import app.thelema.g3d.IUniformArgs
import app.thelema.shader.IShader
import app.thelema.utils.iterate

/** @author zeganstyl */
abstract class ShaderNode: IShaderNode {
    override var shaderOrNull: IShader? = null

    override var entityOrNull: IEntity? = null

    protected open val _inputs: MutableList<IShaderNodeInput<IShaderData?>> = ArrayList(0)
    override val inputs: List<IShaderNodeInput<IShaderData?>>
        get() = _inputs

    protected open val _output: MutableList<IShaderData> = ArrayList(0)

    val attribute: String
        get() = "in"

    val varOut: String
        get() = "out"

    val varIn: String
        get() = "in"

    override fun forEachOutput(block: (output: IShaderData) -> Unit) {
        _output.iterate(block)
    }

    protected open fun output(initial: IShaderData): IShaderData {
        _output.add(initial)
        initial.container = this
        return initial
    }

    override fun prepareToBuild() {}

    override fun shaderCompiled() {}

    override fun bind(uniforms: IUniformArgs) {}

    override fun executionFrag(out: StringBuilder) = Unit
    override fun executionVert(out: StringBuilder) = Unit
    override fun declarationVert(out: StringBuilder) = Unit
    override fun declarationFrag(out: StringBuilder) = Unit

    /** Create shader input named as property */
    @Suppress("UNCHECKED_CAST")
    fun input(default: IShaderData = GLSL.zeroFloat) = ShaderNodeInput(this, default).also {
        _inputs.add(it as IShaderNodeInput<IShaderData?>)
    }

    /** Create shader input named as property */
    fun inputOrNull() = ShaderNodeInputOrNull(this).also {
        _inputs.add(it)
    }
}