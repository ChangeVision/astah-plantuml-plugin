package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram
import net.sourceforge.plantuml.error.PSystemError
import net.sourceforge.plantuml.mindmap.MindMapDiagram
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram
import net.sourceforge.plantuml.statediagram.StateDiagram

object PlantToAstahConverter {
    fun validate(reader: SourceStringReader): ValidationResult {
        val blocks = reader.blocks
        val errors = blocks.map { it.diagram }.filterIsInstance<PSystemError>()

        return when {
            blocks.isEmpty() -> EmptyError
            errors.isNotEmpty() -> SyntaxError(errors)
            else -> ValidationOK
        }
    }

    fun convert(text: String) {
        val reader = SourceStringReader(text)
        reader.blocks.map { it.diagram }.forEachIndexed { index, diagram ->
            when (diagram) {
                is SequenceDiagram -> PlantToAstahSequenceDiagramConverter.convert(diagram, reader, index)
                is DescriptionDiagram -> { // UseCase, Component, Deployment
                }
                is ClassDiagram -> { // Class, Object
                    PlantToAstahClassDiagramConverter.convert(diagram, reader, index)
                }
                is ActivityDiagram -> {
                }
                is StateDiagram -> {
                    PlantToAstahStateDiagramConverter.convert(diagram, reader, index)
                }
                is MindMapDiagram -> {
                }
                else -> System.err.println("Unsupported Diagram : " + diagram.javaClass.name)
            }
        }
    }
}
