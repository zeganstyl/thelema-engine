package app.thelema.test

import app.thelema.ecs.Entity
import app.thelema.ecs.mainLoop
import app.thelema.g3d.Blending
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.material
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.scene
import app.thelema.g3d.transformNode
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.ForwardRenderingPipeline
import app.thelema.shader.SimpleShader3D

class FireShaderTest: Test {
    override fun testMain() {
        val noiseTex = Texture2D("noiseTexture.png")
        val maskTex = Texture2D("fire_mask.png")

        val s = SimpleShader3D {
            fragCode = """
varying vec2 uv;

uniform sampler2D noise_tex;
uniform sampler2D mask_tex;

uniform float time;

const mat2 myt = mat2(.12121212, .13131313, -.13131313, .12121212);
const vec2 mys = vec2(1e4, 1e6);

vec2 rhash(vec2 uv) {
  uv *= myt;
  uv *= mys;
  return fract(fract(uv / mys) * uv);
}

vec3 hash(vec3 p) {
  return fract(sin(vec3(dot(p, vec3(1.0, 57.0, 113.0)),
                        dot(p, vec3(57.0, 113.0, 1.0)),
                        dot(p, vec3(113.0, 1.0, 57.0)))) *
               43758.5453);
}

float voronoi2d(const in vec2 point) {
  vec2 p = floor(point);
  vec2 f = fract(point);
  float res = 0.0;
  for (int j = -1; j <= 1; j++) {
    for (int i = -1; i <= 1; i++) {
      vec2 b = vec2(i, j);
      vec2 r = vec2(b) - f + rhash(p + b);
      res += 1. / pow(dot(r, r), 8.);
    }
  }
  return pow(1. / res, 0.0625);
}

void main() {
    vec2 uv2 = uv * 0.5;
    uv2.y += time;
    
    vec2 uv3 = uv * 2.0;
    uv3.y += time * 4.0;

    float noise_sample = texture2D(noise_tex, uv2).x;
    
    float noise_sample2 = voronoi2d(uv3);
    
    float mask = texture2D(mask_tex, uv).x;

    float f = noise_sample * noise_sample2 * mask;
    gl_FragColor = vec4(4.0, 2.0, 1.0, clamp(f, 0.0, 1.0));
}
"""
        }

        s.bind()
        s["noise_tex"] = 0
        s["mask_tex"] = 1

        var time = 0f

        s.onPrepareShader = { mesh, scene ->
            s["time"] = time
            noiseTex.bind(0)
            maskTex.bind(1)
        }

        Entity {
            makeCurrent()
            scene {
                renderingPipeline = ForwardRenderingPipeline().apply {
                    bloomEnabled = true
                    fxaaEnabled = false
                    motionBlurEnabled = false
                    vignetteEnabled = false
                }
            }
            orbitCameraControl()

            val plane = entity("fire-quad") {
                planeMesh {
                    normal = MATH.Z
                    setSize(1f)
                }
                transformNode {
                    position.y += 0.6f
                    requestTransformUpdate()
                }
                material { shader = s }.alphaMode = Blending.BLEND
            }

            val fireNode = plane.transformNode()

            entity("plane2") {
                planeMesh { setSize(10f) }
                material()
            }

            val tmp = Vec3()

            mainLoop {
                onUpdate {
                    time += it * 0.5f

                    fireNode.worldMatrix.apply {
                        setTransposed3x3From(ActiveCamera.viewMatrix)
                        setWorldUp(getWorldUp(tmp).scl(-1f))
                    }
                }
            }
        }
    }
}