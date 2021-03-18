package com.change_vision.astah.plugins.view

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import java.awt.Font
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class PlantUMLSourceArea(private val textChangeAction: (String) -> Any) : RSyntaxTextArea() {
    init {
        text = stateText
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
                fontSize += e.wheelRotation
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

private val sampleText = """
@startuml
class Activator{
   int attr1
   attr2: int
   void activate()
   deactivate():void
}
class ConvertAction
class AstahToPlantConverter
class ClassConverter
class ConvertMode
class ConvertResult
class LinkConverter
class PlantToAstahClassDiagramConverter
class PlantToAstahConverter
class SVGEntityCollector
class ValidationResult
class ButtonPanel
class PlantDiagramPreviewPanel
class PlantUMLSourceArea
class PlantUMLView
PlantUMLView *-- ConvertMode
PlantUMLView *-- ButtonPanel
PlantUMLView *-- PlantUMLSourceArea
PlantUMLView *-- PlantDiagramPreviewPanel
ButtonPanel *-- ConvertAction
ConvertAction ..> AstahToPlantConverter
ConvertAction ..> PlantToAstahConverter
PlantToAstahConverter *-- PlantToAstahClassDiagramConverter
PlantToAstahConverter *-- SVGEntityCollector
PlantToAstahClassDiagramConverter *-- ClassConverter
PlantToAstahClassDiagramConverter *-- LinkConverter
PlantToAstahClassDiagramConverter --> ConvertResult
PlantToAstahClassDiagramConverter --> ValidationResult
@enduml
""".trimIndent()

private val sequenceText = """
@startuml
Alice -> Bob: Authentication Request
Bob --> Alice: Authentication Response

Alice -> Bob: Another authentication Request
Alice <-- Bob: another authentication Response

actor Foo1
boundary Foo2
control Foo3
entity Foo4
database Foo5
collections Foo6
Foo1 -> Foo2 : To boundary
Foo1 -> Foo3 : To control
Foo1 -> Foo4 : To entity
Foo1 -> Foo5 : To database
Foo1 -> Foo6 : To collections
@enduml
""".trimIndent()

private val stateText = """
    @startuml
    hide empty description
    [*] --> State1
    State1 --> [*]
    State1 : this is a string
    State1 : this is another string

    State1 -> State2
    State2 --> [*]
    @enduml
""".trimIndent()