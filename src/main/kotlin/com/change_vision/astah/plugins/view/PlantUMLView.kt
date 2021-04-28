package com.change_vision.astah.plugins.view

import com.change_vision.jude.api.inf.ui.IPluginExtraTabView
import com.change_vision.jude.api.inf.ui.ISelectionListener
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JSplitPane

class PlantUMLView : JPanel(), IPluginExtraTabView {
    override fun getTitle() = "PlantUML View"
    override fun getDescription() = "Astah - PlantUML Plugin"
    override fun getComponent() = this

    private val previewPanel = PlantDiagramPreviewPanel()
    private val sourceArea = PlantUMLSourcePanel(previewPanel)

    init {
        layout = BorderLayout()
        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            sourceArea,
            previewPanel
        ).also { it.resizeWeight = 0.6 }
        splitPane.isOneTouchExpandable = true
        add(splitPane, BorderLayout.CENTER)
    }

    override fun addSelectionListener(p0: ISelectionListener) {}
    override fun activated() {}
    override fun deactivated() {}
}
