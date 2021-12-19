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

package app.thelema.studio.widget

import app.thelema.studio.SKIN
import app.thelema.g2d.Batch
import app.thelema.math.IMat4
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.TransformDataType
import app.thelema.ui.*

class Mat4Widget: Table(), PropertyProvider<IMat4> {
    val m00Field = FloatField()
    val m01Field = FloatField()
    val m02Field = FloatField()
    val m03Field = FloatField()

    val m10Field = FloatField()
    val m11Field = FloatField()
    val m12Field = FloatField()
    val m13Field = FloatField()

    val m20Field = FloatField()
    val m21Field = FloatField()
    val m22Field = FloatField()
    val m23Field = FloatField()

    val m30Field = FloatField()
    val m31Field = FloatField()
    val m32Field = FloatField()
    val m33Field = FloatField()

    val floatsSection = FloatsWidget {
        addFloatFieldInline(m00Field, SKIN.X, { value.m00 }) { value.m00 = it }
        addFloatFieldInline(m01Field, SKIN.Y, { value.m01 }) { value.m01 = it }
        addFloatFieldInline(m02Field, SKIN.Z, { value.m02 }) { value.m02 = it }
        addFloatFieldInline(m03Field, SKIN.X, { value.m03 }) { value.m03 = it }
        row()

        addFloatFieldInline(m10Field, SKIN.X, { value.m10 }) { value.m10 = it }
        addFloatFieldInline(m11Field, SKIN.Y, { value.m11 }) { value.m11 = it }
        addFloatFieldInline(m12Field, SKIN.Z, { value.m12 }) { value.m12 = it }
        addFloatFieldInline(m13Field, SKIN.Y, { value.m13 }) { value.m13 = it }
        row()

        addFloatFieldInline(m20Field, SKIN.X, { value.m20 }) { value.m20 = it }
        addFloatFieldInline(m21Field, SKIN.Y, { value.m21 }) { value.m21 = it }
        addFloatFieldInline(m22Field, SKIN.Z, { value.m22 }) { value.m22 = it }
        addFloatFieldInline(m23Field, SKIN.Z, { value.m23 }) { value.m23 = it }
        row()

        addFloatFieldInline(m30Field, SKIN.W, { value.m30 }) { value.m30 = it }
        addFloatFieldInline(m31Field, SKIN.W, { value.m31 }) { value.m31 = it }
        addFloatFieldInline(m32Field, SKIN.W, { value.m32 }) { value.m32 = it }
        addFloatFieldInline(m33Field, SKIN.W, { value.m33 }) { value.m33 = it }
        row()
    }

    val matrixDataType = SelectBox<String> {
        items = transformTypes
        getSelected = { value.transformDataType }
        setSelected = { value.transformDataType = it ?: TransformDataType.TRS }
    }

    var value: IMat4 = defaultVec4

    override var set: (value: IMat4) -> Unit = {}
    override var get: () -> IMat4 = { MATH.IdentityMat4 }

    init {
        add(HBox {
            add(Label("Data type:"))
            add(matrixDataType).growX().padLeft(5f)
        }).growX().newRow()
        add(floatsSection).growX().newRow()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        value = get()
        super.draw(batch, parentAlpha)
    }

    companion object {
        val defaultVec4 = Mat4()

        val transformTypes = listOf(
            TransformDataType.None,
            TransformDataType.TRS,
            TransformDataType.Translation,
            TransformDataType.TranslationRotation,
            TransformDataType.TranslationScale
        )
    }
}
