package app.thelema.g3d.particles

import app.thelema.gl.IVertexLayout
import app.thelema.gl.VertexLayout

object Particle {
    val Layout: IVertexLayout = VertexLayout()

    val POSITION_LIFE = Layout.define("PARTICLE_POSITION", 4).apply { divisor = 1 }
    val UV_START = Layout.define("PARTICLE_UV_START", 2).apply { divisor = 1 }
}
