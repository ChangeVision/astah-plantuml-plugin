package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram

object PlantToAstahConverter {
    fun convert(text: String) {
        val reader = SourceStringReader(text)
        reader.blocks.map { it.diagram }.forEach { diagram ->
            when (diagram) {
                is ClassDiagram -> {
                    diagram.leafsvalues.forEach { leaf ->
                        println(leaf.codeGetName)
                    }
                    diagram.links.forEach { link ->
                        println(link.entity1.codeGetName + " " + link.type.toString() + " " + link.entity2.codeGetName)
                    }
                }
                else -> System.err.println("Unsupported Diagram : " + diagram.javaClass.name)
            }
        }
    }
}
