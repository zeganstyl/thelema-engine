package app.thelema.test.gl

import app.thelema.app.APP
import app.thelema.data.DATA
import app.thelema.gl.*
import app.thelema.math.Mat4
import app.thelema.math.Vec2
import app.thelema.test.Test

class UniformBufferBaseTest: Test {
    override fun testMain() {
        val vertexBufferBytes = DATA.bytes(3 * 2 * 4).apply {
            putFloats(
                0f, 0f,
                0f, 1f,
                1f, 0f
            )
            rewind()
        }
        val vertexBuffer = GL.glGenBuffer()
        GL.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        GL.glBufferData(GL_ARRAY_BUFFER, vertexBufferBytes.limit, vertexBufferBytes, GL_STATIC_DRAW)

        val indexBufferBytes = DATA.bytes(3 * 2).apply {
            putShorts(0, 1, 2)
            rewind()
        }
        val indexBuffer = GL.glGenBuffer()
        GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
        GL.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBufferBytes.limit, indexBufferBytes, GL_STATIC_DRAW)

        val uniformBufferBytes = DATA.bytes(8 * 4).apply {
            // put offset
            val vec = Vec2(-0.5f, -0.5f)
            putVec2(vec.x, vec.y)
            position += 8 // vec2 must be aligned as vec4 for std140

            // put color
            putVec4(0f, 1f, 0.5f, 1f)

            rewind()
        }
        val uniformBuffer = GL.glGenBuffer()
        GL.glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer)
        GL.glBufferData(GL_UNIFORM_BUFFER, uniformBufferBytes.limit, uniformBufferBytes, GL_STATIC_DRAW)

        val vao = GL.glGenVertexArrays()

        val glslVer = if (GL.isGLES) "300 es" else "330"

        val positionLocation = 0

        val vertexShaderCode = """
#version $glslVer

layout (location = $positionLocation) in vec2 POSITION;
out vec2 uv;

layout (std140) uniform Uniforms {
    vec2 offset;
    vec4 color;
};

void main() {
    uv = POSITION;
    gl_Position = vec4(POSITION + offset, 0.0, 1.0);
}
"""

        val fragmentShaderCode = """
#version $glslVer

in vec2 uv;
out vec4 FragColor;

layout (std140) uniform Uniforms {
    vec2 offset;
    vec4 color;
};

void main() {
    FragColor = color;
}
"""

        val vertexShader = GL.glCreateShader(GL_VERTEX_SHADER)
        GL.glShaderSource(vertexShader, vertexShaderCode)
        GL.glCompileShader(vertexShader)
        println(GL.glGetShaderInfoLog(vertexShader).ifEmpty { "Vertex shader OK" })

        val fragmentShader = GL.glCreateShader(GL_FRAGMENT_SHADER)
        GL.glShaderSource(fragmentShader, fragmentShaderCode)
        GL.glCompileShader(fragmentShader)
        println(GL.glGetShaderInfoLog(fragmentShader).ifEmpty { "Fragment shader OK" })

        val shaderProgram = GL.glCreateProgram()
        GL.glAttachShader(shaderProgram, vertexShader)
        GL.glAttachShader(shaderProgram, fragmentShader)
        GL.glLinkProgram(shaderProgram)
        println(GL.glGetProgramInfoLog(shaderProgram).ifEmpty { "Shader program OK" })

        val ubIndex = GL.glGetUniformBlockIndex(shaderProgram, "Uniforms")
        val uboBindPoint = 0
        GL.glUniformBlockBinding(shaderProgram, ubIndex, uboBindPoint)
        GL.glBindBufferBase(GL_UNIFORM_BUFFER, uboBindPoint, uniformBuffer)

        GL.glBindVertexArray(vao)
        GL.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)

        GL.glEnableVertexAttribArray(positionLocation)
        GL.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 2 * 4, 0)
        GL.glBindVertexArray(0)

        APP.onRender = {
            GL.glUseProgram(shaderProgram)

            GL.glBindVertexArray(vao)
            GL.glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_SHORT, 0)
            GL.glBindVertexArray(0)
        }
    }
}