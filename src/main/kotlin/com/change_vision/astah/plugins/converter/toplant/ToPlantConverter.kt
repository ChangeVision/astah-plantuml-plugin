package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClassDiagram
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.model.IStateMachineDiagram

object ToPlantConverter {
    private val api = AstahAPI.getAstahAPI()
    fun convert(): String {
        val diagram = api.viewManager.diagramViewManager.currentDiagram
        val sb = StringBuilder()
        sb.appendLine("@startuml")
        when (diagram) {
            is IClassDiagram -> ToPlantClassDiagramConverter.convert(diagram, sb)
            is ISequenceDiagram -> ToPlantSequenceDiagramConverter.convert(diagram, sb)
            is IStateMachineDiagram -> ToPlantStateDiagramConverter.convert(diagram, sb)
        }
        sb.appendLine("@enduml")
        return sb.toString()
    }
}