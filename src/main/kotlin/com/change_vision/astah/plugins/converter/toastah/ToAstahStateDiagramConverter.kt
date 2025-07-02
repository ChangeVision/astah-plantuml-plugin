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

        TransactionManager.beginTransaction()
        try {
            val groupPresentationMap = HashMap<String, INodePresentation>()
            diagram.groups().forEach { group ->
                if ((group.isGroup && group.groupType == GroupType.STATE) || (group.leafType == STATE)) {
                    groupPresentationMap[group.name] = createStatePresentation(group, posMap, groupPresentationMap)
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
                        val presentation = createStatePresentation(leaf, posMap, groupPresentationMap)
                        // 子の状態を保つ場合はgroupとして扱う
                        if (leaf.quark.children.isNotEmpty()) {
                            groupPresentationMap[leaf.name] = presentation
                        }
                        Pair(leaf.name, presentation)
                    }
                    CIRCLE_START -> {
                        val key = if (isRoot(parentContainer)) {
                            "initial"
                        } else {
                            parentContainer.name + ".initial"
                        }
                        val rect = posMap[key]!!
                        // 開始疑似状態 Presentation 作成
                        val presentation = diagramEditor.createInitialPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                        Pair(key, presentation)
                    }
                    CIRCLE_END -> {
                        val key = if (isRoot(parentContainer)) {
                            "final"
                        } else {
                            parentContainer.name + ".final"
                        }
                        val rect = posMap[key]!!
                        // 終了状態 Presentation 作成
                        val presentation = diagramEditor.createFinalState(parentPresentation, Point2D.Float(rect.x, rect.y))
                        val name = leaf.name
                        if (name != "*end*") {
                            (presentation.model as INamedElement).name = name
                            Pair(name, presentation)
                        } else {
                            Pair(key, presentation)
                        }
                    }
                    DEEP_HISTORY -> {
                        val key = if (isRoot(parentContainer)) {
                            "deepHistory"
                        } else {
                            parentContainer.name + ".deepHistory"
                        }
                        val rect = posMap[key]!!
                        val presentation = diagramEditor.createDeepHistoryPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                        val name = leaf.name
                        if (name != "deepHistory") {
                            (presentation.model as INamedElement).name = name
                            Pair(name, presentation)
                        } else {
                            Pair(key, presentation)
                        }
                    }
                    PSEUDO_STATE -> {
                        val key = if (isRoot(parentContainer)) {
                            "history"
                        } else {
                            parentContainer.name + ".history"
                        }
                        val rect = posMap[key]!!
                        val presentation = diagramEditor.createShallowHistoryPseudostate(parentPresentation, Point2D.Float(rect.x, rect.y))
                        val name = leaf.name
                        if (name != "*historical*") {
                            // 浅い履歴疑似状態
                            (presentation.model as INamedElement).name = name
                            Pair(name, presentation)
                        } else {
                            // 浅い履歴疑似状態
                            Pair(key, presentation)
                        }
                    }
                    else -> null
                }
            }.toMap()
            // TODO 一部の線が diagram.links から取得できない。他のモデルからも取得できない。
            diagram.links.forEach { link ->
                // source の Presentation を取得
                val sourceName = link.entity1.name
                val sourceParent = link.entity1.parentContainer
                val source = if (sourceName.endsWith("start*")) {
                    if (isRoot(sourceParent)) {
                        presentationMap["initial"]
                    } else {
                        presentationMap[sourceParent.name + ".initial"]
                    }
                } else if (link.entity1.leafType == DEEP_HISTORY) {
                    if (isRoot(sourceParent)) {
                        presentationMap["deepHistory"]
                    } else {
                        presentationMap[sourceParent.name + ".deepHistory"]
                    }
                } else if (sourceName.endsWith("historical*")) {
                    if (isRoot(sourceParent)) {
                        presentationMap["history"]
                    } else {
                        presentationMap[sourceParent.name + ".history"]
                    }
                } else {
                    groupPresentationMap[sourceName] ?: presentationMap[sourceName]
                }

                // target の Presentation を取得
                val targetName = link.entity2.name
                val targetParent = link.entity2.parentContainer
                val target = if (targetName.endsWith("end*")) {
                    if (isRoot(targetParent)) {
                        presentationMap["final"]
                    } else {
                        presentationMap[targetParent.name + ".final"]
                    }
                } else if (link.entity2.leafType == DEEP_HISTORY) {
                    if (isRoot(targetParent)) {
                        presentationMap["deepHistory"]
                    } else {
                        presentationMap[targetParent.name + ".deepHistory"]
                    }
                } else if (targetName.endsWith("historical*")) {
                    if (isRoot(targetParent)) {
                        presentationMap["history"]
                    } else {
                        presentationMap[targetParent.name + ".history"]
                    }
                } else {
                    groupPresentationMap[targetName] ?: presentationMap[targetName]
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

    private fun isRoot(entity : Entity) : Boolean {
        return entity.isGroup && entity.groupType == GroupType.ROOT
    }
    private fun createStatePresentation(entity : Entity, posMap : Map<String, Rectangle2D.Float>, groupPresentationMap : HashMap<String, INodePresentation>) : INodePresentation {
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
        val location = Point2D.Float(rect.x, rect.y)
        val statePresentation = diagramEditor.createState(entity.name, parentPresentation, location).also {
                entity.bodier.rawBody.toString()
            }
        statePresentation.width = rect.width.toDouble()
        statePresentation.height = rect.height.toDouble()
        return statePresentation
    }
}