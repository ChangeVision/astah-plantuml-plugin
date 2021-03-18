package com.change_vision.astah.plugins.view

import com.change_vision.astah.plugins.converter.toastah.ToAstahConverter
import com.change_vision.astah.plugins.converter.toplant.ToPlantConverter
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Insets
import javax.imageio.ImageIO
import javax.swing.*

class ButtonPanel(sourceArea: PlantUMLSourceArea) : JPanel() {
    private val plantToAstahButton =
        JButton("toAstah").also {
            it.addActionListener { ToAstahConverter.convert(sourceArea.text) }
            it.icon = ImageIcon(ImageIO.read(javaClass.getResource("/toAstah.png")))
            it.margin = Insets(0, 0, 5, 5)
        }
    private val astahToPlantButton =
        JButton("toPlant").also {
            it.addActionListener { sourceArea.text = ToPlantConverter.convert() }
            it.icon = ImageIcon(ImageIO.read(javaClass.getResource("/toPlant.png")))
            it.margin = Insets(0, 0, 5, 20)
        }
    private val statusLabel = JLabel("")

    init {
        layout = BorderLayout()
        add(statusLabel, BorderLayout.WEST)
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        buttonPanel.add(plantToAstahButton)
        buttonPanel.add(astahToPlantButton)
        add(buttonPanel, BorderLayout.EAST)
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