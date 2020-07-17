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
import org.ksdfv.thelema.test.shaders.*
import org.ksdfv.thelema.test.sound.SoundWavTest

/** @author zeganstyl */
abstract class Tests {
    val groups = ArrayList<TestCategory>()

    val meshes = TestCategory(
        "Mesh",
        MeshTest(),
        MeshCubeTest(),
        ScreenQuadTest(),
        PlaneMeshBuilderTest(),
        BoxMeshBuilderTest(),
        FrustumMeshBuilderTest(),
        InstancingTest()
    )

    val shaders = TestCategory(
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

    val other = TestCategory(
        "Other",
        Texture2DTest(),
        FrameBufferTest(),
        SoundWavTest(),
        KeyboardTest(),
        MouseTest(),
        CascadedShadowMatricesTest(),
        SkyboxTest()
    )

    init {
        groups.add(meshes)
        groups.add(shaders)
        groups.add(other)
    }
}
