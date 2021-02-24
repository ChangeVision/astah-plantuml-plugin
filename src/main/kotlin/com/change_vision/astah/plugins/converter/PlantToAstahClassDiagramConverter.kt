package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IDiagram
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.ILeaf
import net.sourceforge.plantuml.cucadiagram.Link
import java.awt.geom.Point2D


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
        val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
        val foundDiagramList = projectAccessor.findElements(IDiagram::class.java, diagramName)
        val classDiagram =
            when {
                foundDiagramList.isNotEmpty() -> {
                    foundDiagramList.first() as IDiagram
                }
                else -> {
                    TransactionManager.beginTransaction()
                    try {
                        diagramEditor.createClassDiagram(projectAccessor.project, diagramName)
                            .also { TransactionManager.endTransaction() }
                    } catch (e: BadTransactionException) {
                        TransactionManager.abortTransaction()
                        return
                    }
                }
            }

        // convert presentations
        val api = AstahAPI.getAstahAPI()
        TransactionManager.beginTransaction()
        try {
            val viewElementMap = entityMap.keys.map { entity ->
                val viewElement =
                    diagramEditor.createNodePresentation(
                        entityMap[entity],
                        Point2D.Float(0f, 0f)
                    )
                Pair(entity, viewElement)
            }.toMap()
            linkConvertResults
                .filterIsInstance<Success<Pair<Link, IAssociation>>>()
                .map { it.convertPair }
                .forEach { link ->
                    diagramEditor.createLinkPresentation(
                        link.second,
                        viewElementMap[link.first.entity1],
                        viewElementMap[link.first.entity2]
                    )
                }
            api.viewManager.diagramViewManager.open(classDiagram)
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }
    }
}