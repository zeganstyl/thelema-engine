package app.thelema.studio

import app.thelema.g3d.ITransformNode
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.utils.Color
import kotlin.math.abs

// https://github.com/CedricGuillemet/LibGizmo/blob/master/src/libgizmo/GizmoTransformMove.cpp
// TODO
class TranslationGizmo {
    var worldMatrix: IMat4? = null

    var node: ITransformNode? = null

    var isVisible: Boolean = true
}
