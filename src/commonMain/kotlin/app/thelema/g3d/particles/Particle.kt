package app.thelema.g3d.particles

import app.thelema.gl.Vertex

object Particle {
    val LIFE_TIME = Vertex.define("PARTICLE_LIFE_TIME", 1).apply { divisor = 1 }
    val MAX_LIFE_TIME = Vertex.define("PARTICLE_MAX_LIFE_TIME", 1).apply { divisor = 1 }
    val UV_START = Vertex.define("PARTICLE_UV_START", 1).apply { divisor = 1 }
}