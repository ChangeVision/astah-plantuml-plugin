package com.change_vision.astah.plugins.action

import com.change_vision.astah.plugins.view.PlantUMLSourceArea
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

private const val convertButtonText = "convert"

class ConvertPlantToAstahAction(private val sourceArea: PlantUMLSourceArea) : AbstractAction(convertButtonText) {
    override fun actionPerformed(e: ActionEvent?) {
        TODO("Not yet implemented")
    }
}