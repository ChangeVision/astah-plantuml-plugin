package com.change_vision.astah.plugins.action

import com.change_vision.astah.plugins.converter.AstahToPlantConverter
import com.change_vision.astah.plugins.converter.ConvertMode
import com.change_vision.astah.plugins.converter.PlantToAstahConverter
import com.change_vision.astah.plugins.view.PlantUMLSourceArea
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

private const val convertButtonText = "convert"

class ConvertAction(
    private val sourceArea: PlantUMLSourceArea,
    private val getCurrentMode: () -> ConvertMode
) : AbstractAction(convertButtonText) {
    override fun actionPerformed(e: ActionEvent?) {
        when (getCurrentMode()) {
            ConvertMode.PlantToAstah -> PlantToAstahConverter.convert(sourceArea.text)
            ConvertMode.AstahToPlant -> sourceArea.text = AstahToPlantConverter.convert()
        }
        PlantToAstahConverter.convert(sourceArea.text)
    }
}