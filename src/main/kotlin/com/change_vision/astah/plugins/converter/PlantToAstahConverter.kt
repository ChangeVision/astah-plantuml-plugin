package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram

object PlantToAstahConverter {
    fun convert(text: String) {
        val reader = SourceStringReader(text)
        reader.blocks.map { it.diagram }.forEach { diagram ->
            when (diagram) {
                is ClassDiagram -> PlantToAstahClassDiagramConverter.convert(diagram)
                else -> System.err.println("Unsupported Diagram : " + diagram.javaClass.name)
            }
        }
    }
}
