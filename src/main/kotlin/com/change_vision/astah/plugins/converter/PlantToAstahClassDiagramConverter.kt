package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.model.IClass
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.ILeaf


object PlantToAstahClassDiagramConverter {
    fun convert(diagram: ClassDiagram) {
        // convert to astah model
        val leafConvertResults =
            ClassConverter.createAstahModelElements(diagram.leafsvalues)
        val entityMap = leafConvertResults
            .filterIsInstance<Success<Pair<ILeaf, IClass>>>()
            .map { it.convertPair }.toMap()

        val linkConvertResults = LinkConverter.createAstahLinkElements(diagram.links, entityMap)

        // create diagram


        // convert presentations
    }
}