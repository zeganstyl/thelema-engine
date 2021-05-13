package app.thelema.android

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import app.thelema.data.IByteData
import app.thelema.jvm.data.JvmByteBuffer
import app.thelema.net.WS
import app.thelema.net.WebSocketAdapter

class KtorWebSocket: WebSocketAdapter() {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    var session: WebSocketSession? = null

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) {
        super.open(url, error, opened)

        GlobalScope.launch {
            readyStateInternal = WS.CONNECTING

            try {
                client.webSocket(
                    urlString = url,
                    block = {
                        session = this

                        readyStateInternal = WS.OPEN
                        opened()

                        incoming.consumeEach {
                            when (it) {
                                is Frame.Text -> {
                                    val text = it.readText()
                                    for (i in listeners.indices) {
                                        listeners[i].textReceived(text)
                                    }
                                }
                                is Frame.Binary -> {
                                    val bytes = JvmByteBuffer(it.buffer)
                                    for (i in listeners.indices) {
                                        listeners[i].bytesReceived(bytes)
                                    }
                                }
                                else -> {}
                            }
                        }

                        readyStateInternal = WS.CLOSING
                    }
                )

                session = null
                readyStateInternal = WS.CLOSED
                for (i in listeners.indices) {
                    listeners[i].closed()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun send(text: String) {
        runBlocking {
            session?.send(text)
        }
    }

    override fun send(bytes: IByteData) {
        val session = session
        if (session != null) {
            val array = ByteArray(bytes.limit)
            for (i in array.indices) {
                array[i] = bytes[i]
            }

            runBlocking {
                session.send(array)
            }
        }
    }

    override fun close(code: Int, reason: String) {
        runBlocking {
            session?.close()
        }
    }

    override fun close() {
        runBlocking {
            session?.close()
        }
    }
}