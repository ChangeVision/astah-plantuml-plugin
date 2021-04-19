package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.ILeaf
import net.sourceforge.plantuml.cucadiagram.Link
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JMenu


object ToAstahClassDiagramConverter {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
    fun convert(diagram: ClassDiagram, reader: SourceStringReader, index: Int) {
        // convert to astah model
        val leafConvertResults =
            ClassConverter.createAstahModelElements(diagram.leafsvalues)
        val entityMap = leafConvertResults
            .filterIsInstance<Success<Pair<ILeaf, IClass>>>()
            .map { it.convertPair }.toMap()

        val linkConvertResults = LinkConverter.createAstahLinkElements(diagram.links, entityMap)
        val linkMap = linkConvertResults.filterIsInstance<Success<Pair<Link, INamedElement>>>()
            .map { it.convertPair }.toMap()

        // create diagram
        val classDiagram = createOrGetDiagram(index, DiagramKind.ClassDiagram)

        // convert presentations
        val positionMap = SVGEntityCollector.collectSvgPosition(reader, index)
        createPresentations(entityMap, linkMap, positionMap)

        if (classDiagram != null) {
            api.viewManager.diagramViewManager.open(classDiagram)
            val frame = api.viewManager.mainFrame
            (api.viewManager.mainFrame.rootPane.menuBar.getMenu(3).getItem(4) as JMenu).getItem(0)
        }
    }

    private fun createPresentations(
        entityMap: Map<ILeaf, IClass>,
        linkMap: Map<Link, INamedElement>,
        positionMap: Map<String, Rectangle2D.Float>
    ) {
        TransactionManager.beginTransaction()
        try {
            val viewElementMap = entityMap.keys.mapNotNull { entity ->
                val code = "class " + entity.code
                if (positionMap.containsKey(code)) {
                    val position = positionMap[code]!!
                    val viewElement =
                        diagramEditor.createNodePresentation(
                            entityMap[entity],
                            Point2D.Float(position.x, position.y)
                        )
                    Pair(entity, viewElement)
                } else {
                    null
                }
            }.toMap()
            linkMap.forEach { (link, model) ->
                diagramEditor.createLinkPresentation(
                    model,
                    viewElementMap[link.entity1],
                    viewElementMap[link.entity2]
                )
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }
    }
}