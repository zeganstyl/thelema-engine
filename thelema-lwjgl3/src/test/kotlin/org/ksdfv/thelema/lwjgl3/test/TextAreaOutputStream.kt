package org.ksdfv.thelema.lwjgl3.test

import java.io.OutputStream
import java.io.PrintStream
import javax.swing.JTextArea

/** https://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea */
class TextAreaOutputStream(var textArea: JTextArea, val oldStream: PrintStream): OutputStream() {
    override fun write(b: Int) {
        oldStream.write(b)
        // redirects data to the text area
        textArea.append(b.toChar().toString())
        // scrolls the text area to the end of data
        textArea.caretPosition = textArea.document.length
    }
}
