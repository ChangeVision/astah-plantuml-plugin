package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.action.ConvertAction
import com.change_vision.astah.plugins.converter.ConvertMode
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.*

class ButtonPanel(convertAction: ConvertAction, modeChangeCallback: (ConvertMode) -> Any) : JPanel() {
    private val generateButton = JButton(convertAction)
    private val statusLabel = JLabel("")
    private val modeComboBox = JComboBox(ConvertMode.values())
        .also { comboBox ->
            comboBox.addActionListener {
                modeChangeCallback(comboBox.selectedItem as ConvertMode)
            }
            comboBox.renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
                ): Component {
                    val displayString = (value as ConvertMode).comboboxText
                    return super.getListCellRendererComponent(list, displayString, index, isSelected, cellHasFocus)
                }
            }
        }

    init {
        layout = BorderLayout(10, 10)
        add(statusLabel, BorderLayout.WEST)
        add(modeComboBox, BorderLayout.CENTER)
        add(generateButton, BorderLayout.EAST)
    }

    fun setMessage(message: String) {
        statusLabel.foreground =
            when (message) {
                "OK" -> Color(20, 200, 50)
                else -> Color.RED
            }
        statusLabel.text = message
    }
}