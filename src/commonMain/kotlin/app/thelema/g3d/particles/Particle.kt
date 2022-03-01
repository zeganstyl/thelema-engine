package app.thelema.g3d.particles

import app.thelema.gl.IVertexLayout
import app.thelema.gl.VertexLayout

object Particle {
    val Layout: IVertexLayout = VertexLayout()

    val POSITION = Layout.define("POSITION", 3)
    val COLOR = Layout.define("COLOR", 4)
    val TEXCOORD_0 = Layout.define("TEXCOORD_0", 2)
    val NORMAL = Layout.define("NORMAL", 3)
    val TANGENT = Layout.define("TANGENT", 4)
    val PARTICLE_POSITION = Layout.define("PARTICLE_POSITION", 3).apply { divisor = 1 }
    val LIFE_TIME = Layout.define("PARTICLE_LIFE_TIME", 1).apply { divisor = 1 }
    val MAX_LIFE_TIME = Layout.define("PARTICLE_MAX_LIFE_TIME", 1).apply { divisor = 1 }
    val UV_START = Layout.define("PARTICLE_UV_START", 2).apply { divisor = 1 }
}
