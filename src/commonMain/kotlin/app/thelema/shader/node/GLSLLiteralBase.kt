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

abstract class GLSLLiteralBase: IShaderData, IShaderNode {
    override var scope: Int
        get() = GLSLScope.Inline
        set(_) {}

    override var entityOrNull: IEntity? = null

    override var shaderOrNull: IShader?
        get() = null
        set(_) {}

    override val shader: IShader
        get() = throw IllegalStateException("You can't use shader from GLSL literal node")

    override var name: String
        get() = ""
        set(_) {}

    override var container: IShaderNode?
        get() = this
        set(_) {}

    override val id: Int
        get() = 0

    override var isUsed: Boolean
        get() = false
        set(_) {}

    override val inputs: List<IShaderNodeInput<IShaderData?>>
        get() = emptyList()

    override fun getComponentPropertyValue(name: String): Any? {
        if (name.isEmpty()) return this
        return super.getComponentPropertyValue(name)
    }

    override fun prepareToBuild() {}
    override fun shaderCompiled() {}
    override fun bind(uniforms: IUniformArgs) {}
    override fun executionVert(out: StringBuilder) {}
    override fun executionFrag(out: StringBuilder) {}
    override fun declarationVert(out: StringBuilder) {}
    override fun declarationFrag(out: StringBuilder) {}

    override fun forEachOutput(block: (output: IShaderData) -> Unit) {
        block(this)
    }

    protected fun str(float: Float): String {
        val v = float.toString()
        return if (v.contains('.')) v else "$v.0"
    }
}
