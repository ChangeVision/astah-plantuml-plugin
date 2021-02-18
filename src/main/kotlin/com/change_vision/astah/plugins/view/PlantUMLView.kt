package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.action.ConvertPlantToAstahAction
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView
import com.change_vision.jude.api.inf.ui.ISelectionListener
import java.awt.BorderLayout
import javax.swing.JPanel

class PlantUMLView : JPanel(), IPluginExtraTabView {
    override fun getTitle() = "PlantUML View"
    override fun getDescription() = "Astah - PlantUML Plugin"
    override fun getComponent() = this

    private val sourceArea = PlantUMLSourceArea()
    private val convertAction = ConvertPlantToAstahAction(sourceArea)
    private val buttonPanel = ButtonPanel(convertAction)

    init {
        layout = BorderLayout()
        add(buttonPanel, BorderLayout.NORTH)
        add(sourceArea, BorderLayout.CENTER)
    }

    override fun addSelectionListener(p0: ISelectionListener) {}
    override fun activated() {}
    override fun deactivated() {}
}
