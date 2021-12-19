package app.thelema.ecs

import app.thelema.fs.FS
import kotlin.test.Test
import kotlin.test.assertEquals

class FileTest {
    @Test
    fun readInternalFile() {
        val internal = FS.internal("test-file.txt")
        internal.readBytes(
            ready = { bytes ->
                assertEquals(113, bytes[0].toInt())
                assertEquals(119, bytes[1].toInt())
                assertEquals(101, bytes[2].toInt())
                assertEquals(114, bytes[3].toInt())
                assertEquals(116, bytes[4].toInt())
                assertEquals(121, bytes[5].toInt())
            },
            error = { status ->
                throw IllegalStateException("internal readBytes: FAILED (status = $status)")
            }
        )
        internal.readText(
            ready = { text ->
                assertEquals("qwerty", text)
            },
            error = { status ->
                throw IllegalStateException("internal readText: FAILED (status = $status)")
            }
        )
    }

    @Test
    fun writeLocalFile() {
        if (FS.writeAccess) {
            val local = FS.local("local-file-test.txt")
            local.writeText("qwerty123")
            local.readBytes(
                ready = { bytes ->
                    assertEquals(113, bytes[0].toInt())
                    assertEquals(119, bytes[1].toInt())
                    assertEquals(101, bytes[2].toInt())
                    assertEquals(114, bytes[3].toInt())
                    assertEquals(116, bytes[4].toInt())
                    assertEquals(121, bytes[5].toInt())
                    assertEquals(49, bytes[6].toInt())
                    assertEquals(50, bytes[7].toInt())
                    assertEquals(51, bytes[8].toInt())
                },
                error = { status ->
                    throw IllegalStateException("local readBytes: FAILED (status = $status)")
                }
            )
            local.readText(
                ready = { text ->
                    assertEquals("qwerty123", text)
                },
                error = { status ->
                    throw IllegalStateException("local readText: FAILED (status = $status)")
                }
            )
            local.delete()
        }
    }
}