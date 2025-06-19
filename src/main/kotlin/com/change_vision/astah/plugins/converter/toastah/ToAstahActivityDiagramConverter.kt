package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IActivityDiagram
import com.change_vision.jude.api.inf.model.IFlow
import com.change_vision.jude.api.inf.presentation.INodePresentation
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.abel.LeafType
import net.sourceforge.plantuml.klimt.creole.Display
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

object ToAstahActivityDiagramConverter {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.activityDiagramEditor
    fun convert(diagram: ActivityDiagram, reader: SourceStringReader, index: Int) {
        // delete diagram if exists //TODO
        projectAccessor.findElements(IActivityDiagram::class.java, "Activity_$index").let {
            if (it.isNotEmpty()) {
                TransactionManager.beginTransaction()
                projectAccessor.modelEditorFactory.basicModelEditor.delete(it.first())
                TransactionManager.endTransaction()
            }
        }

        // create diagram
        val astahDiagram = createOrGetDiagram(index, DiagramKind.ActivityDiagram)
        val posMap = SVGEntityCollector.collectSvgPosition(reader, index)

        TransactionManager.beginTransaction()
        try {
            val presentationMap = diagram.leafs().mapNotNull { leaf ->
                when (leaf.leafType) {
                    LeafType.ACTIVITY -> {
                        val rect = when {
                            posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val statePresentation =
                            diagramEditor.createAction(leaf.name, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        Pair(leaf.name, statePresentation)
                    }
                    LeafType.SYNCHRO_BAR -> {
                        val rect = when {
                            posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }

                        val barPresentation = if(SVGEntityCollector.isFork(leaf)){
                            diagramEditor.createForkNode(null, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        }else{
                            diagramEditor.createJoinNode(null, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        }
                        Pair(leaf.name, barPresentation)
                    }
                    LeafType.CIRCLE_START -> {
                        val rect = posMap["initial"]!!
                        Pair(
                            "initial",
                            diagramEditor.createInitialNode("initial", Point2D.Float(rect.x + 10, rect.y + 10))
                        )
                    }
                    LeafType.CIRCLE_END -> {
                        val rect = posMap["final"]!!
                        Pair("final", diagramEditor.createFinalNode("final", Point2D.Float(rect.x + 10, rect.y + 10)))
                    }
                    else -> null
                }
            }.toMap()


            diagram.links.forEach { link ->
                val source = findPresentation(link.entity1.name, presentationMap)
                val target = findPresentation(link.entity2.name, presentationMap)
                val linkPs = when {
                    link.entity1.name == "end" -> diagramEditor.createFlow(target, source)
                    link.entity2.name == "start" -> diagramEditor.createFlow(target, source)
                    else -> diagramEditor.createFlow(source, target)
                }


                val display : Display = link.label
                (linkPs.model as? IFlow)?.let { flow ->
                    if(!Display.isNull(display))
                        flow.guard = display.toString().removePrefix("[").removeSuffix("]")
                }
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }

        astahDiagram?.let { api.viewManager.diagramViewManager.open(it) }
    }

    fun findPresentation(name: String, map: Map<String, INodePresentation>) = when (name) {
        "start" -> map["initial"]!!
        "end" -> map["final"]!!
        else -> map[name]
    }

}