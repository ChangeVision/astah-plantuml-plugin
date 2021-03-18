package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IStateMachineDiagram
import com.change_vision.jude.api.inf.model.ITransition
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.cucadiagram.LeafType
import net.sourceforge.plantuml.statediagram.StateDiagram
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.regex.Pattern


object PlantToAstahStateDiagramConverter {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.stateMachineDiagramEditor
    fun convert(diagram: StateDiagram, reader: SourceStringReader, index: Int) {
        // delete diagram if exists //TODO
        projectAccessor.findElements(IStateMachineDiagram::class.java, "SequenceDiagram_$index").let {
            if (it.isNotEmpty()) {
                TransactionManager.beginTransaction()
                projectAccessor.modelEditorFactory.basicModelEditor.delete(it.first())
                TransactionManager.endTransaction()
            }
        }

        // create diagram
        val astahDiagram = createOrGetDiagram(index, DiagramKind.StateDiagram)
        val posMap = SVGEntityCollector.collectSvgPosition(reader, index)

        TransactionManager.beginTransaction()
        try {
            val presentationMap = diagram.leafsvalues.mapNotNull { leaf ->
                when (leaf.leafType) {
                    LeafType.STATE -> {
                        val rect = when {
                            posMap.containsKey(leaf.codeGetName) -> posMap[leaf.codeGetName]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val statePresentation =
                            diagramEditor.createState(leaf.codeGetName, null, Point2D.Float(rect.x, rect.y)).also {
                                leaf.bodier.rawBody.toString()
                            }
                        Pair(leaf.codeGetName, statePresentation)
                    }
                    LeafType.CIRCLE_START -> {
                        val rect = posMap["initial"]!!
                        val initialPresentation =
                            diagramEditor.createInitialPseudostate(null, Point2D.Float(rect.x + 10, rect.y + 10))
                        Pair("initial", initialPresentation)
                    }
                    LeafType.CIRCLE_END -> {
                        val rect = posMap["final"]!!
                        val finalPresentation =
                            diagramEditor.createFinalState(null, Point2D.Float(rect.x + 10, rect.y + 10))
                        Pair("final", finalPresentation)
                    }
                    else -> null
                }
            }.toMap()
            diagram.links.forEach { link ->
                val source = when (link.entity1.codeGetName) {
                    "*start" -> presentationMap["initial"]!!
                    else -> presentationMap[link.entity1.codeGetName]
                }
                val target = when (link.entity2.codeGetName) {
                    "*end" -> presentationMap["final"]!!
                    else -> presentationMap[link.entity2.codeGetName]
                }
                val transitionLabelRegex =
                    Pattern.compile("""(<?event>\w+)(?:\[(<?guard>\w)\])?(?:/(<?action>\w+))?""")
                diagramEditor.createTransition(source, target)
                    .also { transition ->
                        val label = link.label.toString()
                        val matcher = transitionLabelRegex.matcher(label)
                        if (transition.label.contains("トリガー")) {
                            when {
                                link.label.isWhite -> transition.label = ""
                                matcher.matches() -> {
                                    val model = ((transition.model) as ITransition)
                                    model.event = matcher.group("event") ?: ""
                                    model.guard = matcher.group("guard") ?: ""
                                    model.action = matcher.group("action") ?: ""
                                }
                                else -> transition.label = label
                            }
                        }
                    }
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }

        astahDiagram?.let { api.viewManager.diagramViewManager.open(it) }
    }
}