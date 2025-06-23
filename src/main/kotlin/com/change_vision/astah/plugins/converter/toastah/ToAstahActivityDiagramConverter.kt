package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IActivityDiagram
import com.change_vision.jude.api.inf.model.IFlow
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
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
                        val activityName = leaf.name
                        val rect = when {
                            posMap.containsKey(activityName) -> posMap[activityName]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val displayName = leaf.display.joinToString("\n")
                        val actionPresentation =
                            diagramEditor.createAction(
                                displayName, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        if(actionPresentation is INamedElement && displayName != activityName){
                            actionPresentation.alias1 = activityName
                        }
                        Pair(activityName, actionPresentation)
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
                    LeafType.BRANCH -> {
                        val rect = when {
                            posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val decisionNodePresentation =
                            diagramEditor.createDecisionMergeNode(null, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        Pair(leaf.name, decisionNodePresentation)
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
                    LeafType.NOTE -> {
                        val rect = when {
                            posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        Pair(leaf.name, diagramEditor.createNote(leaf.display.joinToString("\n"), Point2D.Float(rect.x, rect.y)))
                    }
                    else -> null
                }
            }.toMap()

            val regex = """-(left|right|up|down)?->""".toRegex()
            val sources = reader.blocks.firstOrNull()?.data

            diagram.links.forEach { link ->
                val source = findPresentation(link.entity1.name, presentationMap)
                val target = findPresentation(link.entity2.name, presentationMap)

                val code = sources?.getOrNull(link.location.position)?.toString().orEmpty()
                val matchResult = regex.find(code,0)

                val isReverse = when (matchResult?.value) {
                    "-left->", "-up->" -> true
                    "-right->", "-down->" -> false
                    else -> false
                }

                var linkPs : ILinkPresentation?

                if (source?.type == "Note" ) {
                    linkPs = diagramEditor.createNoteAnchor(source,target)
                }else if(target?.type == "Note"){
                    linkPs = diagramEditor.createNoteAnchor(target,source)
                }else{
                    if(isReverse){
                        linkPs = diagramEditor.createFlow(target, source)
                    }else{
                        linkPs = diagramEditor.createFlow(source, target)
                    }
                }

                val display = link.label
                val quantifier1 = link.quantifier1
                val quantifier2 = link.quantifier2

                val guardText = buildString {
                    display?.takeIf { !Display.isNull(it) }?.let {
                        append(it.toString().removePrefix("[").removeSuffix("]"))
                    }
                    quantifier1?.let { append(it) }
                    quantifier2?.let { append(it) }
                }

                if (guardText.isNotEmpty()) {
                    (linkPs.model as? IFlow)?.guard = guardText
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