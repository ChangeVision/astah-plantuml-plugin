package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClassDiagram
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.model.IStateMachineDiagram

object AstahToPlantConverter {
    private val api = AstahAPI.getAstahAPI()
    fun convert(): String {
        val diagram = api.viewManager.diagramViewManager.currentDiagram
        val sb = StringBuilder()
        sb.appendLine("@startuml")
        when (diagram) {
            is IClassDiagram -> AstahToPlantClassDiagramConverter.convert(diagram, sb)
            is ISequenceDiagram -> AstahToPlantSequenceDiagramConverter.convert(diagram, sb)
            is IStateMachineDiagram -> AstahToPlantStateDiagramConverter.convert(diagram, sb)
        }
        sb.appendLine("@enduml")
        return sb.toString()
    }
}