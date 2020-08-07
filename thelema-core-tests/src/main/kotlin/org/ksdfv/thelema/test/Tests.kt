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

import org.ksdfv.thelema.test.mesh.*
import org.ksdfv.thelema.test.phys.BoxShapeTest
import org.ksdfv.thelema.test.phys.SphereShapeTest
import org.ksdfv.thelema.test.phys.TrimeshShapeTest
import org.ksdfv.thelema.test.shaders.*
import org.ksdfv.thelema.test.shaders.glsl.*
import org.ksdfv.thelema.test.sound.SoundWavTest

/** @author zeganstyl */
open class Tests {
    val groups = ArrayList<TestGroup>()

    val meshes = TestGroup(
        "Mesh",
        MeshTest(),
        MeshCubeTest(),
        PlaneMeshBuilderTest(),
        BoxMeshBuilderTest(),
        FrustumMeshBuilderTest(),
        InstancingTest()
    )

    val shaders = TestGroup(
        "Shader",
        BloomBaseTest(),
        BloomTest(),
        CascadedShadowMappingBaseTest(),
        FXAATest(),
        GBufferBaseTest(),
        MotionBlurBaseTest(),
        ShadowMappingBaseTest(),
        SSAOBaseTest(),
        ThresholdTest()
    )

    val glslNodes = TestGroup(
        "GLSL nodes",
        CascadedShadowMappingTest(),
        EmissionBloomTest(),
        GBufferTest(),
        MotionBlurTest(),
        SkyboxVertexNodeTest(),
        SSAOTest(),
        VelocityNodeTest(),
        VertexNodeTest()
    )

    val other = TestGroup(
        "Other",
        Texture2DTest(),
        FrameBufferTest(),
        SoundWavTest(),
        KeyboardTest(),
        MouseTest(),
        CascadedShadowMatricesTest(),
        SkyboxTest(),
        GLTFLoaderAnimTest()
    )

    val physics = TestGroup(
        "Physics",
        BoxShapeTest(),
        SphereShapeTest(),
        TrimeshShapeTest()
    )

    init {
        groups.add(meshes)
        groups.add(shaders)
        groups.add(glslNodes)
        groups.add(other)
        groups.add(physics)
    }
}
