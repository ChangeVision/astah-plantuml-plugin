package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.converter.EmptyError
import com.change_vision.astah.plugins.converter.SyntaxError
import com.change_vision.astah.plugins.converter.ValidationOK
import com.change_vision.astah.plugins.converter.toastah.classstructure.ToAstahConverter
import com.change_vision.astah.plugins.converter.toplant.ToPlantConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import net.sourceforge.plantuml.SourceStringReader
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextAreaEditorKit
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.DefaultEditorKit
import javax.swing.text.JTextComponent


class PlantUMLSourcePanel(private val previewPanel: PlantDiagramPreviewPanel) : JPanel() {
    private val statusBar = StatusBar()

    init {
        // 複数のクラスローダでRTextAreaを利用する場合のバグ回避。
        // https://github.com/bobbylight/RSyntaxTextArea/issues/269
        JTextComponent.removeKeymap("RTextAreaKeymap")
        val sourceArea = PlantUMLSourceArea { textChangeAction(it) }
        UIManager.put("RSyntaxTextAreaUI.actionMap", null)
        UIManager.put("RSyntaxTextAreaUI.inputMap", null)
        UIManager.put("RTextAreaUI.actionMap", null)
        UIManager.put("RTextAreaUI.inputMap", null)

        val buttonPanel = ButtonPanel(sourceArea)
        layout = BorderLayout()
        add(buttonPanel, BorderLayout.NORTH)
        val textScrollPane = RTextScrollPane(sourceArea).also {
            it.lineNumbersEnabled = true
        }

        add(textScrollPane, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        textChangeAction(sourceArea.text)
    }

    /**
     * 入力テキストの監視し、Validationを行う。
     * イベント毎にValidationを行うと重いので、
     * テキスト変更後に200ms変更がなかった場合のみValidationを行う。
     * Validation結果は、buttonPanelのステータスに表示
     */
    private var job: Job? = null
    private fun textChangeAction(text: String) {
        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Swing) {
            delay(200)
            val plantReader = SourceStringReader(text)
            val validationResult = ToAstahConverter.validate(plantReader)
            val statusMessage = when (validationResult) {
                is ValidationOK -> "OK"
                is EmptyError -> "Empty"
                is SyntaxError -> "syntax error in " + validationResult.errors
                    .joinToString(",") { error -> (error.lineLocation.position + 1).toString() }
            }
            statusBar.setMessage(statusMessage)
            if (validationResult == ValidationOK) {
                previewPanel.updateImage(plantReader)
            }
        }
    }
}

private class ButtonPanel(private val sourceArea: PlantUMLSourceArea) : JPanel() {
    private val plantToAstahButton =
        JButton("toAstah").also {
            it.addActionListener { ToAstahConverter.convert(sourceArea.text) }
            it.icon = ImageIcon(ImageIO.read(javaClass.getResource("/toAstah.png")))
            it.margin = Insets(0, 0, 0, 0)
        }
    private val astahToPlantButton =
        JButton("toPlant").also {
            it.addActionListener { sourceArea.text = ToPlantConverter.convert() }
            it.icon = ImageIcon(ImageIO.read(javaClass.getResource("/toPlant.png")))
            it.margin = Insets(0, 0, 0, 0)
        }

    init {
        layout = BorderLayout(10, 10)

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        buttonPanel.add(Box.createRigidArea(Dimension(10, 22)))
        buttonPanel.add(plantToAstahButton)
        buttonPanel.add(Box.createRigidArea(Dimension(10, 0)))
        buttonPanel.add(astahToPlantButton)
        add(buttonPanel, BorderLayout.CENTER)
    }
}

private class PlantUMLSourceArea(private val textChangeListener: (String) -> Any) : RSyntaxTextArea() {
    init {
        text = sampleText
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                textChangeListener(text)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                textChangeListener(text)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                textChangeListener(text)
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
        setForeground(UIManager.getColor("TextArea.foresground"));
        setBackground(UIManager.getColor("TextArea.background"));
        if (isMacOSX()) {
            addMacDefaultKeybindings()
        }
    }

    private fun isMacOSX() =
        System.getProperty("os.name").lowercase().startsWith("mac os x")

    private fun addMacDefaultKeybindings() {
        //move
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.forwardAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.backwardAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.upAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.downAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.beginLineAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK),
            DefaultEditorKit.endLineAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
            RTextAreaEditorKit.pasteAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
            RTextAreaEditorKit.deleteNextCharAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK),
            RTextAreaEditorKit.deletePrevCharAction
        )

        //shift + move ---> select
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionForwardAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionBackwardAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionUpAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionDownAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionBeginLineAction
        )
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            DefaultEditorKit.selectionEndLineAction
        )
    }
}

private class StatusBar : JPanel() {
    private val statusText = JLabel()

    init {
        layout = BorderLayout(10, 10)
        add(statusText, BorderLayout.WEST)
    }

    fun setMessage(message: String) {
        statusText.foreground =
            when (message) {
                "OK" -> UIManager.getColor("Label.foreground")
                else -> Color.RED.brighter()
            }
        statusText.text = "syntax : $message"
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

