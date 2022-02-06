package app.thelema.test.shader

import app.thelema.app.APP
import app.thelema.gl.GL_CLAMP_TO_EDGE
import app.thelema.gl.ScreenQuad
import app.thelema.img.Texture2D
import app.thelema.math.Vec2
import app.thelema.shader.post.PostShader
import app.thelema.test.Test

class HeightMapToNormalMapTest : Test {
    override fun testMain() {
        val shader = PostShader(
            flipY = true,
            fragCode = """
in vec2 uv;
out vec4 FragColor;

uniform sampler2D tex;
uniform vec2 texel_size;
uniform vec2 scale;

void main() {
    float top = texture(tex, vec2(uv.x, uv.y + texel_size.y)).r;
    float bottom = texture(tex, vec2(uv.x, uv.y - texel_size.y)).r;
    float left = texture(tex, vec2(uv.x - texel_size.x, uv.y)).r;
    float right = texture(tex, vec2(uv.x + texel_size.x, uv.y)).r;
    
    vec3 n = normalize(vec3((right - left) * scale.x, (top - bottom) * scale.y, 1.0));

    float r = left - right;
    FragColor = vec4(n * 0.5 + 0.5, 1.0);
}
"""
        )

        val tex = Texture2D("terrain/heightmap.png")
        tex.sWrap = GL_CLAMP_TO_EDGE
        tex.tWrap = GL_CLAMP_TO_EDGE

        val step = 1f
        val scale = 30f

        shader.bind()
        shader["tex"] = 0
        shader["texel_size"] = Vec2(step / tex.width, step / tex.height)
        shader["scale"] = Vec2(scale, scale)

        APP.onRender = {
            tex.bind(0)
            ScreenQuad.render(shader)
        }
    }
}