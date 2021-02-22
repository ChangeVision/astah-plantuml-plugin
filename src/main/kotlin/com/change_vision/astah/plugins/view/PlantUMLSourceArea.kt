package com.change_vision.astah.plugins.view

import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class PlantUMLSourceArea(private val textChangeAction: (String) -> Any) : JTextArea() {
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
    }
}

private val sampleText = """
    @startuml
    class Dummy {
      String data
      void methods()
    }

    class Flight {
       flightNumber : Integer
       departureTime : Date
    }
    interface Hoge
    interface Foo
    Dummy -- Flight
    AAA -- BBB
    @enduml
""".trimIndent()
