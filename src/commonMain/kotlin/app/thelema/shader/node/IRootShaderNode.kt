package app.thelema.shader.node

import app.thelema.ecs.IEntityComponent

interface IRootShaderNode: IEntityComponent {
    var proxy: IShaderNode?
}