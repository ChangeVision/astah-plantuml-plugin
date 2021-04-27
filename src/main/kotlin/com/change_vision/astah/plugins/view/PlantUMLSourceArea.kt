package com.change_vision.astah.plugins.view

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import java.awt.Font
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class PlantUMLSourceArea(private val textChangeAction: (String) -> Any) : RSyntaxTextArea() {
    init {
        text = sampleText
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                textChangeAction(text)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                textChangeAction(text)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                textChangeAction(text)
            }
        })
        font = Font("Terminal", Font.PLAIN, 12)
        var fontSize = font.size
        addMouseWheelListener { e ->
            if (e.isControlDown) {
                fontSize -= e.wheelRotation
                fontSize = when {
                    fontSize > 50 -> 50
                    fontSize < 8 -> 8
                    else -> fontSize
                }
                font = Font(font.fontName, font.style, fontSize)
            } else {
                parent.dispatchEvent(e)
            }
        }
    }
}

val sampleText =
    """
        @startuml
        class SampleClass {
          int a
        }
        @enduml
    """.trimIndent()

