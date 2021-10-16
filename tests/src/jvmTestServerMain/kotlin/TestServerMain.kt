import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.TransformNode
import app.thelema.jvm.JvmGraphiclessApp
import java.time.Duration

fun main() {
    val app = JvmGraphiclessApp()

    var sceneAction: Boolean = false

    Entity {
        entity("node") {
            component<ITransformNode> {

            }
        }
    }

    app.onUpdate = {

    }

    Thread {
        app.startLoop()
    }.start()

    embeddedServer(Netty, port = 8888) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
            masking = false
        }

        routing {
            get("/") {
                call.respondText("test server is running")
                //call.respondRedirect("/index.html", permanent = true)
            }

            get("/text") {
                call.respondText("qwerty")
            }

            get("/bytes") {
                call.respondBytes(byteArrayOf(4, 2))
            }

            post("/text") {
                val text = call.receiveText()
                call.respondText {
                    if (text != "qwerty") {
                        "body must be 'qwerty' but was $text"
                    } else {
                        "post text OK"
                    }
                }
            }

            post("/bytes") {
                val bytes = call.receiveStream().readAllBytes()
                call.respondText {
                    if (bytes[0].toInt() != 4 || bytes[1].toInt() != 2) {
                        "bytes must be [4, 2] but was [${bytes[0]}, ${bytes[1]}]"
                    } else {
                        "OK"
                    }
                }
            }

            post("/form") {
                var result = "post form OK"
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> if (part.value != "qwerty") {
                                    result = "name must be 'qwerty' but was ${part.value}"
                                }
                                "float" -> {
                                    val float = part.value.toFloatOrNull()
                                    if (float != 0.123f) {
                                        result = "float must be 0.123 but was ${part.value} ($float)"
                                    }
                                }
                            }
                        }
                        is PartData.BinaryItem -> {
                            when (part.name) {
                                "bytes" -> {
                                    val bytes = part.provider().readBytes()
                                    if (bytes[0].toInt() != 4 || bytes[1].toInt() != 2) {
                                        result = "bytes must be [4, 2] but was [${bytes[0]}, ${bytes[1]}]"
                                    }
                                }
                            }
                        }
                    }

                    part.dispose()
                }
                call.respondText { result }
            }

            // This defines a websocket `/ws` route that allows a protocol upgrade to convert a HTTP request/response request
            // into a bidirectional packetized connection.
            webSocket("/") { // this: WebSocketSession ->
                try {
                    incoming.consumeEach { frame ->
                        send(frame)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                }
            }
        }
    }.start(wait = true)
}