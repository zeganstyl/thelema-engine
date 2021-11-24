package app.thelema.test.gl

import app.thelema.app.APP
import app.thelema.data.DATA
import app.thelema.gl.*
import app.thelema.test.Test

class TriangleBaseTest: Test {
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

        val vertexShaderCode = """
attribute vec2 POSITION;
varying vec2 uv;

void main() {
    uv = POSITION;
    gl_Position = vec4(POSITION, 0.0, 1.0);
}
"""

        val fragmentShaderCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 1.0, 1.0);
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

        APP.onRender = {
            GL.glUseProgram(shaderProgram)

            val positionAttribute = GL.glGetAttribLocation(shaderProgram, "POSITION")
            GL.glEnableVertexAttribArray(positionAttribute)
            GL.glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, 2 * 4, 0)

            GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
            GL.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
            GL.glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_SHORT, 0)
        }
    }
}