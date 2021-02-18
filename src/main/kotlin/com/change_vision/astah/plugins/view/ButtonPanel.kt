package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.action.ConvertPlantToAstahAction
import javax.swing.JButton
import javax.swing.JPanel

class ButtonPanel(convertAction: ConvertPlantToAstahAction) : JPanel() {
    private val generateButton = JButton(convertAction)

    init {
        add(generateButton)
    }
}