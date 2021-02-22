package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.action.ConvertPlantToAstahAction
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class ButtonPanel(convertAction: ConvertPlantToAstahAction) : JPanel() {
    private val generateButton = JButton(convertAction)
    private val statusLabel = JLabel("")

    init {
        layout = BorderLayout(10, 10)
        add(statusLabel, BorderLayout.WEST)
        add(generateButton, BorderLayout.EAST)
    }

    fun setMessage(message: String) {
        statusLabel.text = message
    }
}