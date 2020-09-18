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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.test.audio.SoundOggTest
import org.ksdfv.thelema.test.audio.SoundWavTest
import org.ksdfv.thelema.test.g3d.GLTFLoaderAnimTest
import org.ksdfv.thelema.test.img.FrameBufferTest
import org.ksdfv.thelema.test.img.GBufferBaseTest
import org.ksdfv.thelema.test.img.SkyboxTest
import org.ksdfv.thelema.test.img.Texture2DTest
import org.ksdfv.thelema.test.input.KeyboardTest
import org.ksdfv.thelema.test.input.MouseTest
import org.ksdfv.thelema.test.json.JsonTest
import org.ksdfv.thelema.test.mesh.*
import org.ksdfv.thelema.test.phys.BoxShapeTest
import org.ksdfv.thelema.test.phys.SphereShapeTest
import org.ksdfv.thelema.test.phys.TrimeshShapeTest
import org.ksdfv.thelema.test.shader.CascadedShadowMappingBaseTest
import org.ksdfv.thelema.test.shader.ShadowMappingBaseTest
import org.ksdfv.thelema.test.shader.node.*
import org.ksdfv.thelema.test.shader.post.*

/** @author zeganstyl */
open class Tests {
    val groups = ArrayList<TestGroup>()

    val meshes = TestGroup(
        "Mesh",
        ScreenQuadTest(),
        MeshCubeTest(),
        PlaneMeshBuilderTest(),
        BoxMeshBuilderTest(),
        FrustumMeshBuilderTest(),
        InstancingTest()
    )

    val other = TestGroup(
        "Other",
        JsonTest(),
        CascadedShadowMatricesTest(),
        GLTFLoaderAnimTest()
    )

    val shaders = TestGroup(
        "Shader",
        BloomTest(),
        CascadedShadowMappingBaseTest(),
        FXAATest(),
        MotionBlurBaseTest(),
        ShadowMappingBaseTest(),
        SSAOBaseTest(),
        ThresholdTest(),
        SobelBaseTest()
    )

    val shaderNodes = TestGroup(
        "Shader nodes",
        CascadedShadowMappingTest(),
        EmissionBloomNodeTest(),
        GBufferTest(),
        MotionBlurTest(),
        SkyboxVertexNodeTest(),
        SSAOTest()
    )

    val img = TestGroup(
        "Image",
        Texture2DTest(),
        FrameBufferTest(),
        GBufferBaseTest(),
        SkyboxTest(),
    )

    val physics = TestGroup(
        "Physics",
        BoxShapeTest(),
        SphereShapeTest(),
        TrimeshShapeTest()
    )

    val audio = TestGroup(
        "Audio",
        SoundWavTest(),
        SoundOggTest(),
    )

    val input = TestGroup(
        "Input",
        KeyboardTest(),
        MouseTest(),
    )

    init {
        groups.add(meshes)
        groups.add(other)
        groups.add(shaders)
        groups.add(shaderNodes)
        groups.add(img)
        groups.add(physics)
        groups.add(audio)
        groups.add(input)
    }
}
