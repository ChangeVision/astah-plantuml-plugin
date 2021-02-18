package com.change_vision.astah.plugins.view

import com.change_vision.jude.api.inf.ui.IPluginExtraTabView
import com.change_vision.jude.api.inf.ui.ISelectionListener
import javax.swing.JPanel

class PlantUMLView : JPanel(), IPluginExtraTabView {
    override fun getTitle() = "PlantUML View"
    override fun getDescription() = "Astah - PlantUML Plugin"
    override fun getComponent() = this

    override fun addSelectionListener(p0: ISelectionListener) {}
    override fun activated() {}
    override fun deactivated() {}
}
