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

import app.thelema.g3d.IObject3D
import app.thelema.g3d.IScene
import app.thelema.json.IJsonObjectIO
import app.thelema.gl.IMesh
import app.thelema.shader.IShader

/** @author zeganstyl */
interface IShaderNode: IJsonObjectIO {
    val name: String

    val classId: String

    var shaderOrNull: IShader?

    val shader: IShader
        get() = shaderOrNull!!

    /** Do not remove or add values from external class, just replace value by index */
    val input: Map<String, IShaderData>

    /** Contain names of all input parameters and their types. Key - input name, value - input type */
    val inputForm: Map<String, Int>

    val output: Map<String, IShaderData>

    /** Input data for [input] must be linked through this method */
    fun setInput(name: String, value: IShaderData?)

    /** Before code will be generated */
    fun prepareToBuild()

    /** Here you can get uniform locations, set sampler handles and etc */
    fun shaderCompiled()

    /** Set some uniforms, like lights */
    fun prepareToDrawScene(scene: IScene)

    /** Set some uniforms, like transformations */
    fun prepareObjectData(obj: IObject3D)

    /** Rebind textures to units, set blending function, set face culling and etc */
    fun prepareToDrawMesh(mesh: IMesh)

    /** Vertex shader code, that will be executed in main */
    fun executionVert(out: StringBuilder)

    /** Fragment shader code, that will be executed in main */
    fun executionFrag(out: StringBuilder)

    /** Vertex shader code, that will be in global section */
    fun declarationVert(out: StringBuilder)

    /** Fragment shader code, that will be in global section */
    fun declarationFrag(out: StringBuilder)

    fun set(other: IShaderNode): IShaderNode
}