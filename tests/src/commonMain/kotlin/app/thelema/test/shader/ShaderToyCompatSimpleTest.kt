package app.thelema.test.shader

import app.thelema.app.APP
import app.thelema.app.AppListener
import app.thelema.data.DATA
import app.thelema.gl.*
import app.thelema.img.Image
import app.thelema.img.Texture2D
import app.thelema.math.Vec2
import app.thelema.shader.post.PostShader
import app.thelema.test.Test
import kotlin.random.Random

class ShaderToyCompatSimpleTest: Test {
    override fun testMain() {
        // https://www.shadertoy.com/view/4lfXRf
        val shader = PostShader(
            fragCode = """
in vec2 uv;
out vec4 FragColor;
uniform sampler2D iChannel0;
uniform float iTime;
uniform vec2 iResolution;

#define NUM_PARTICLES	75
#define NUM_FIREWORKS	5

vec3 pow3(vec3 v, float p)
{
    return pow(abs(v), vec3(p));
}

vec2 noise(vec2 tc)
{
    return (2.*texture(iChannel0, tc).xy-1.).xy;
}

vec3 fireworks(vec2 p)
{
    vec3 color = vec3(0., 0., 0.);
    
    for(int fw = 0; fw < NUM_FIREWORKS; fw++)
    {
        vec2 pos = noise(vec2(0.82, 0.11)*float(fw))*1.5;
    	float time = mod(iTime*3., 6.*(1.+noise(vec2(0.123, 0.987)*float(fw)).x));
        for(int i = 0; i < NUM_PARTICLES; i++)
    	{
        	vec2 dir = noise(vec2(0.512, 0.133)*float(i));
            dir.y -=time * 0.1;
            float term = 1./length(p-pos-dir*time)/50.;
            color += pow3(vec3(
                term * noise(vec2(0.123, 0.133)*float(i)).y,
                0.8 * term * noise(vec2(0.533, 0.133)*float(i)).x,
                0.5 * term * noise(vec2(0.512, 0.133)*float(i)).x),
                          1.25);
        }
    }
    return color;
}

vec3 flag(vec2 p)
{
    vec3 color;
    
    p.y += sin(p.x*1.3+iTime)*0.1;
    
    if(p.y > 0.) 	color = vec3(1.);
    else			color = vec3(1., 0., 0.);
    
    color *= sin(3.1415/2. + p.x*1.3+iTime)*0.3 + 0.7;
    
    return color * 0.15;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
	vec2 p = 2. * fragCoord / iResolution.xy - 1.;
    p.x *= iResolution.x / iResolution.y;
    
    vec3 color = fireworks(p) + flag(p);
    fragColor = vec4(color, 1.);
}

void main() {
    mainImage(FragColor, uv * iResolution);
}
"""
        )

        shader.bind()
        shader["iChannel0"] = 0
        shader["iResolution"] = Vec2(APP.width.toFloat(), APP.height.toFloat())

        var time = 0f

        val noise = Texture2D {
            image = Image {
                height = 256
                width = 256
                pixelChannelType = GL_UNSIGNED_BYTE
                internalFormat = GL_RGB8
                pixelFormat = GL_RGB
                val bytesNum = width * height * 3
                bytes = DATA.bytes(bytesNum) {
                    put(Random.nextBytes(bytesNum))
                }
            }
        }

        APP.addListener(object : AppListener {
            override fun update(delta: Float) {
                time += delta
            }

            override fun resized(width: Int, height: Int) {
                shader.bind()
                shader["iResolution"] = Vec2(APP.width.toFloat(), APP.height.toFloat())
            }

            override fun render() {
                shader["iTime"] = time
                shader.bind()
                noise.bind(0)

                ScreenQuad.render(shader)
            }
        })
    }
}