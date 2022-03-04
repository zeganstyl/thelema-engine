package app.thelema.g3d.particles

import app.thelema.gl.IVertexLayout
import app.thelema.gl.VertexLayout

object Particle {
    val Layout: IVertexLayout = VertexLayout()

    // TODO make particle position as 4d vector with last component as a life time
    val POSITION = Layout.define("PARTICLE_POSITION", 3).apply { divisor = 1 }
    val LIFE_TIME = Layout.define("PARTICLE_LIFE_TIME", 1).apply { divisor = 1 }
    val UV_START = Layout.define("PARTICLE_UV_START", 2).apply { divisor = 1 }
}
