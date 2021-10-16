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

package app.thelema.gltf

import app.thelema.gl.GL_LINEAR
import app.thelema.gl.GL_LINEAR_MIPMAP_LINEAR
import app.thelema.math.IVec4
import app.thelema.shader.node.PBRNode

/** @author zeganstyl */
class GLTFConf() {
    constructor(block: GLTFConf.() -> Unit): this() { block(this) }

    /** Store file buffers in RAM */
    var saveFileBuffersInCPUMem: Boolean = false
    /** Store texture buffers in RAM */
    var saveTexturesInCPUMem: Boolean = false
    /** Store mesh buffers in RAM */
    var saveMeshesInCPUMem: Boolean = false

    /** Velocity rendering may be used for motion blur */
    var setupVelocityShader: Boolean = false

    var receiveShadows: Boolean = false

    var pbrConf: PBRNode.() -> Unit = {}

    var setupDepthRendering: Boolean = true

    /** Merge vertex attribute data in one VBO for each primitive */
    var mergeVertexAttributes: Boolean = false

    var loadMeshes: Boolean = true
    var loadImages: Boolean = true
    var loadAnimations: Boolean = true
    var generateShaders: Boolean = true

    var ibl: Boolean = false
    var iblMaxMipLevels: Int = 5

    /** If not null, will be created shader for solid color rendering channel */
    var solidColor: IVec4? = null

    /** Setup pipeline for deferred shading */
    var setupGBufferShader: Boolean = false

    /** Default shader version */
    var shaderVersion: Int = if (setupGBufferShader) 330 else 110

    var defaultTextureMinFilter = GL_LINEAR_MIPMAP_LINEAR
    var defaultTextureMagFilter = GL_LINEAR

    /** You can make some changes to shader graph, before shaders will be compiled */
    var configureMaterials: (material: GLTFMaterial) -> Unit = {}

    operator fun invoke(block: GLTFConf.() -> Unit) = block(this)
}