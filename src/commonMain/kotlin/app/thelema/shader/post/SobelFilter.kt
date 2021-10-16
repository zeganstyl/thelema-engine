package app.thelema.shader.post

import app.thelema.gl.GL
import app.thelema.gl.ScreenQuad
import app.thelema.img.IFrameBuffer
import app.thelema.img.ITexture
import app.thelema.math.Vec3

class SobelFilter: PostShader(
    fragCode = """
// Sobel Edge Detection Filter
// GLSL Fragment Shader
// Implementation by Patrick Hebron

uniform sampler2D	tex;
uniform float 		width;
uniform float 		height;
varying vec2 uv;

uniform vec3 edgeColor;

void main(void) 
{
	vec3 n[9];

    float w = 1.0 / width;
	float h = 1.0 / height;

	n[0] = texture2D(tex, uv + vec2( -w, -h)).xyz;
	n[1] = texture2D(tex, uv + vec2(0.0, -h)).xyz;
	n[2] = texture2D(tex, uv + vec2(  w, -h)).xyz;
	n[3] = texture2D(tex, uv + vec2( -w, 0.0)).xyz;
	n[4] = texture2D(tex, uv).xyz;
	n[5] = texture2D(tex, uv + vec2(  w, 0.0)).xyz;
	n[6] = texture2D(tex, uv + vec2( -w, h)).xyz;
	n[7] = texture2D(tex, uv + vec2(0.0, h)).xyz;
	n[8] = texture2D(tex, uv + vec2(  w, h)).xyz;

	vec3 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
  	vec3 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
	vec3 sobel = clamp((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v), vec3(0.0), vec3(1.0));
    gl_FragColor = vec4(edgeColor * sobel.xyz, 1.0);
}"""
) {
    val edgeColor = Vec3(1f, 1f, 1f)

    init {
        bind()
        set("tex", 0)
    }

    override fun render(inputMap: ITexture, out: IFrameBuffer?) {
        bind()
        inputMap.bind(0)
        set("edgeColor", edgeColor)
        this["width"] = (out?.width ?: GL.mainFrameBufferWidth).toFloat()
        this["height"] = (out?.height ?: GL.mainFrameBufferHeight).toFloat()
        ScreenQuad.render(this, out)
    }
}