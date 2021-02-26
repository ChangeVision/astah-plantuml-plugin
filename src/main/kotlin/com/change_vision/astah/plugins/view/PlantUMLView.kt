package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.converter.EmptyError
import com.change_vision.astah.plugins.converter.PlantToAstahConverter
import com.change_vision.astah.plugins.converter.SyntaxError
import com.change_vision.astah.plugins.converter.ValidationOK
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView
import com.change_vision.jude.api.inf.ui.ISelectionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import net.sourceforge.plantuml.SourceStringReader
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JSplitPane

class PlantUMLView : JPanel(), IPluginExtraTabView {
    override fun getTitle() = "PlantUML View"
    override fun getDescription() = "Astah - PlantUML Plugin"
    override fun getComponent() = this

    private val sourceArea = PlantUMLSourceArea { textChangeAction(it) }
    private val buttonPanel = ButtonPanel(sourceArea)
    private val previewPanel = PlantDiagramPreviewPanel()
    
    init {
        layout = BorderLayout()
        add(buttonPanel, BorderLayout.NORTH)
        val textScrollPane = RTextScrollPane(sourceArea).also {
            it.lineNumbersEnabled = true
        }
        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            textScrollPane,
            previewPanel
        ).also { it.resizeWeight = 0.6 }
        add(splitPane, BorderLayout.CENTER)

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
            val validationResult = PlantToAstahConverter.validate(plantReader)
            val statusMessage = when (validationResult) {
                is ValidationOK -> "OK"
                is EmptyError -> "Empty"
                is SyntaxError -> "syntax error in " + validationResult.errors
                    .map { (it.lineLocation.position + 1).toString() }.joinToString { it }
            }
            buttonPanel.setMessage(statusMessage)
            if (validationResult == ValidationOK) {
                previewPanel.updateImage(plantReader)
            }
        }
    }

    override fun addSelectionListener(p0: ISelectionListener) {}
    override fun activated() {}
    override fun deactivated() {}
}
