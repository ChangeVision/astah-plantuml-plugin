package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IDiagram
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.ILeaf


object PlantToAstahClassDiagramConverter {
    fun convert(diagram: ClassDiagram, reader: SourceStringReader, index: Int) {
        // convert to astah model
        val leafConvertResults =
            ClassConverter.createAstahModelElements(diagram.leafsvalues)
        val entityMap = leafConvertResults
            .filterIsInstance<Success<Pair<ILeaf, IClass>>>()
            .map { it.convertPair }.toMap()

        val linkConvertResults = LinkConverter.createAstahLinkElements(diagram.links, entityMap)

        // create diagram
        val diagramName = "ClassDiagram_$index"
        val projectAccessor = AstahAPI.getAstahAPI().projectAccessor
        if (projectAccessor.findElements(IDiagram::class.java, diagramName).isEmpty()) {
            val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
            TransactionManager.beginTransaction()
            diagramEditor.createClassDiagram(projectAccessor.project, diagramName)
            TransactionManager.endTransaction()
        }

        // convert presentations
    }
}