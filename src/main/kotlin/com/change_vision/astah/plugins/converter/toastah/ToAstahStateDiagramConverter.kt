package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IStateMachineDiagram
import com.change_vision.jude.api.inf.model.ITransition
import com.change_vision.jude.api.inf.presentation.INodePresentation
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.GroupType
import net.sourceforge.plantuml.abel.LeafType.STATE
import net.sourceforge.plantuml.abel.LeafType.CIRCLE_START
import net.sourceforge.plantuml.abel.LeafType.CIRCLE_END
import net.sourceforge.plantuml.abel.LeafType.DEEP_HISTORY
import net.sourceforge.plantuml.abel.LeafType.PSEUDO_STATE
import net.sourceforge.plantuml.statediagram.StateDiagram
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.regex.Pattern


object ToAstahStateDiagramConverter {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.stateMachineDiagramEditor
    fun convert(diagram: StateDiagram, reader: SourceStringReader, index: Int) {
        // 作成予定の図と同名の図("StateDiagram_$index")があれば削除して、常に新規作成するようにする
        projectAccessor.findElements(IStateMachineDiagram::class.java, "StateDiagram_$index").let {
            if (it.isNotEmpty()) {
                TransactionManager.beginTransaction()
                projectAccessor.modelEditorFactory.basicModelEditor.delete(it.first())
                TransactionManager.endTransaction()
            }
        }

        // create diagram
        val astahDiagram = createOrGetDiagram(index, DiagramKind.StateDiagram)
        val posMap = SVGEntityCollector.collectSvgPosition(reader, index)

        // TODO 初期状態・終了状態は図中にそれぞれ1つだけある場合にのみ対応
        TransactionManager.beginTransaction()
        try {
            val groupPresentationMap = HashMap<String, INodePresentation>()
            diagram.groups().forEach { group ->
                if ((group.isGroup && group.groupType == GroupType.STATE) || (group.leafType == STATE)) {
                    groupPresentationMap[group.name] =createStatePresentation(group, posMap, groupPresentationMap)
                }
            }
            val presentationMap = diagram.leafs().mapNotNull { leaf ->
                val parentContainer = leaf.parentContainer
                var parentPresentation : INodePresentation? = null
                if (groupPresentationMap.containsKey(parentContainer.name)) {
                    parentPresentation = groupPresentationMap[parentContainer.name]
                }
                when (leaf.leafType) {
                    STATE -> {
                        Pair(leaf.name, createStatePresentation(leaf, posMap, groupPresentationMap))
                    }
                    CIRCLE_START -> {
                        val rect = posMap["initial"]!!
                        // 開始疑似状態 Presentation 作成
                        val initialPresentation =
                            diagramEditor.createInitialPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                        Pair("initial", initialPresentation)
                    }
                    CIRCLE_END -> {
                        val rect = posMap["final"]!!
                        // 終了状態 Presentation 作成
                        val finalPresentation =
                            diagramEditor.createFinalState(parentPresentation, Point2D.Float(rect.x, rect.y))
                        val name = leaf.name
                        if (name != "*end*") {
                            (finalPresentation.model as INamedElement).name = name
                            Pair(name, finalPresentation)
                        } else {
                            Pair("final", finalPresentation)
                        }
                    }
                    DEEP_HISTORY -> {
                        val rect = posMap["deepHistory"]!!
                        val deepHistoryPresentation = diagramEditor.createDeepHistoryPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                        val name = leaf.name
                        if (name != "deepHistory") {
                            (deepHistoryPresentation.model as INamedElement).name = name
                            Pair(name, deepHistoryPresentation)
                        } else {
                            Pair("deepHistory", deepHistoryPresentation)
                        }
                    }
                    PSEUDO_STATE -> {
                        val name = leaf.name
                        if (name == "*historical*") {
                            // 浅い履歴疑似状態
                            val rect = posMap["history"]!!
                            val historyPresentation = diagramEditor.createShallowHistoryPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                            Pair(name, historyPresentation)
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }.toMap()
            diagram.links.forEach { link ->
                // source の Presentation を取得
                val source = when (link.entity1.name) {
                    "*start*" -> presentationMap["initial"]!!
                    else -> groupPresentationMap[link.entity1.name] ?: presentationMap[link.entity1.name]
                }
                // target の Presentation を取得
                val target = when (link.entity2.name) {
                    "*end*" -> presentationMap["final"]!!
                    else -> groupPresentationMap[link.entity2.name] ?: presentationMap[link.entity2.name]
                }
                val transitionLabelRegex =
                    Pattern.compile("""(<?event>\w+)(?:\[(<?guard>\w)])?(?:/(<?action>\w+))?""")
                // Transition の Presentation の作成
                if (source != null && target != null) {
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
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }

        astahDiagram?.let { api.viewManager.diagramViewManager.open(it) }
    }

    private fun createStatePresentation(entity : Entity, posMap : Map<String, Rectangle2D. Float>, groupPresentationMap : HashMap<String, INodePresentation>) : INodePresentation {
        val rect = when {
            posMap.containsKey(entity.name) -> posMap[entity.name]!!
            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
        }
        // 状態 Presentation の作成
        val parentContainer = entity.parentContainer
        var parentPresentation: INodePresentation? = null
        if (groupPresentationMap.containsKey(parentContainer.name)) {
            parentPresentation = groupPresentationMap[parentContainer.name]
        }
        val statePresentation = diagramEditor.createState(entity.name, parentPresentation, Point2D.Float(rect.x, rect.y)).also {
                entity.bodier.rawBody.toString()
            }
        statePresentation.width = rect.width.toDouble()
        statePresentation.height = rect.height.toDouble()
        return statePresentation
    }
}