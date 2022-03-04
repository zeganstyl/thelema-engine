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

package app.thelema.test

import app.thelema.test.audio.SoundOggTest
import app.thelema.test.audio.SoundWavTest
import app.thelema.test.g3d.BoxMeshTest
import app.thelema.test.g3d.FrustumMeshTest
import app.thelema.test.g3d.MeshCubeTest
import app.thelema.test.g3d.PlaneMeshTest
import app.thelema.test.g3d.gltf.GLTFMultiUVTest
import app.thelema.test.img.FrameBufferTest
import app.thelema.test.img.GBufferBaseTest
import app.thelema.test.img.SkyboxBaseTest
import app.thelema.test.img.Texture2DTest
import app.thelema.test.input.KeyboardTest
import app.thelema.test.input.MouseTest
import app.thelema.test.json.JsonTest
import app.thelema.test.gl.*
import app.thelema.test.phys.BoxShapeTest
import app.thelema.test.phys.SphereShapeTest
import app.thelema.test.phys.TrimeshShapeTest
import app.thelema.test.shader.CascadedShadowMappingBaseTest
import app.thelema.test.shader.ShadowMappingBaseTest
import app.thelema.test.shader.node.*
import app.thelema.test.shader.post.*

/** @author zeganstyl */
open class Tests: TestGroup("Core") {
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
        EmissionBloomTest(),
        GBufferTest(),
        MotionBlurTest(),
        SkyboxShaderNodeTest(),
        SSAOTest()
    )

    val img = TestGroup(
        "Image",
        Texture2DTest(),
        FrameBufferTest(),
        GBufferBaseTest(),
        SkyboxBaseTest(),
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
        group(
            "Mesh",
            ScreenQuadTest(),
            MeshCubeTest(),
            PlaneMeshTest(),
            BoxMeshTest(),
            FrustumMeshTest(),
            InstancedMeshTest()
        )

        group(
            "Other",
            JsonTest(),
            CascadedShadowMatricesTest(),
            GLTFMultiUVTest()
        )

        groups.add(shaders)
        groups.add(shaderNodes)
        groups.add(img)
        groups.add(physics)
        groups.add(audio)
        groups.add(input)
    }
}
