package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.EmptyError
import com.change_vision.astah.plugins.converter.SyntaxError
import com.change_vision.astah.plugins.converter.ValidationOK
import com.change_vision.astah.plugins.converter.ValidationResult
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram
import net.sourceforge.plantuml.error.PSystemError
import net.sourceforge.plantuml.mindmap.MindMapDiagram
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram
import net.sourceforge.plantuml.statediagram.StateDiagram

object ToAstahConverter {
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
        reader.blocks.map { it.diagram }.forEachIndexed { index: Int, diagram ->
            when (diagram) {
                is SequenceDiagram -> ToAstahSequenceDiagramConverter.convert(diagram, index)
                is DescriptionDiagram -> { // UseCase, Component, Deployment
                }
                is ClassDiagram -> { // Class, Object
                    ToAstahClassDiagramConverter.convert(diagram, reader, index)
                }
                is ActivityDiagram -> {
                    ToAstahActivityDiagramConverter.convert(diagram, reader, index)
                }
                is StateDiagram -> {
                    ToAstahStateDiagramConverter.convert(diagram, reader, index)
                }
                is MindMapDiagram -> {
                }
                else -> System.err.println("Unsupported Diagram : " + diagram.javaClass.name)
            }
        }
    }
}
