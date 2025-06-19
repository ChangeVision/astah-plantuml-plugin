package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IUseCaseDiagram
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.abel.LeafType
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.regex.Pattern

object ToAstahUseCaseDiagramConverter {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.useCaseDiagramEditor
    private val modelEditor = projectAccessor.modelEditorFactory.useCaseModelEditor

    fun convert(diagram: DescriptionDiagram, reader: SourceStringReader, index: Int) {
        // 作成予定の図と同名の図を探して削除する(繰り返し実行する場合は図を削除して作り直す)
        projectAccessor.findElements(IUseCaseDiagram::class.java, "UseCaseDiagram_$index").let {
            if (it.isNotEmpty()) {
                TransactionManager.beginTransaction()
                projectAccessor.modelEditorFactory.basicModelEditor.delete(it.first())
                TransactionManager.endTransaction()
            }
        }

        // ユースケース図の作成
        val astahDiagram = createOrGetDiagram(index, DiagramKind.UseCaseDiagram)
        // PlantUML 上での各図要素の位置と大きさを取得
        val posMap = SVGEntityCollector.collectSvgPosition(reader, index)

        TransactionManager.beginTransaction()
        try {
            val leafs = diagram.leafs()
            val presentationMap = diagram.leafs().mapNotNull { leaf ->
                when (leaf.leafType) {
                    LeafType.USECASE -> {
                        val display = leaf.display.get(0)
                        val rect = when {
                            posMap.containsKey(display) -> posMap[display]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val sb = StringBuilder()
                        for (i in 0 until leaf.display.size()) {
                            if (i == 0) {
                                sb.append(leaf.display.get(0))
                            } else {
                                sb.appendLine("")
                                sb.append(leaf.display.get(i))
                            }
                        }
                        val model = modelEditor.createUseCase(projectAccessor.project, sb.toString())
                        val useCasePresentation = diagramEditor.createNodePresentation(model, Point2D.Float(rect.x, rect.y))
                        Pair(leaf.name, useCasePresentation)
                    }
                    LeafType.USECASE_BUSINESS -> {
                        // TODO ビジネスユースケースにする
                        val display = leaf.display.get(0)
                        val rect = when {
                            posMap.containsKey(display) -> posMap[display]!!
                            else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                        }
                        val sb = StringBuilder()
                        for (i in 0 until leaf.display.size()) {
                            if (i == 0) {
                                sb.append(leaf.display.get(0))
                            } else {
                                sb.appendLine("")
                                sb.append(leaf.display.get(i))
                            }
                        }
                        val model = modelEditor.createUseCase(projectAccessor.project, sb.toString())
                        model.addStereotype("business")
                        val useCasePresentation = diagramEditor.createNodePresentation(model, Point2D.Float(rect.x, rect.y))
                        Pair(leaf.name, useCasePresentation)
                    }
                    LeafType.DESCRIPTION -> {
                        val symbol = leaf.uSymbol
                        val symbolStyle = symbol.sNames
                        if (symbolStyle.isNotEmpty()) {
                            if (symbolStyle[0].name == "actor") {
                                // アクター
                                val rect = when {
                                    posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                                    else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                                }
                                val actor = modelEditor.createActor(projectAccessor.project, leaf.name)
                                val actorPresentation = diagramEditor.createNodePresentation(actor, Point2D.Float(rect.x, rect.y))
                                Pair(leaf.name, actorPresentation)
                            } else if (symbolStyle[0].name == "business") {
                                // ビジネスアクター
                                val rect = when {
                                    posMap.containsKey(leaf.name) -> posMap[leaf.name]!!
                                    else -> Rectangle2D.Float(30f, 30f, 30f, 30f)
                                }
                                val actor = modelEditor.createActor(projectAccessor.project, leaf.name)
                                actor.addStereotype("business")
                                val actorPresentation = diagramEditor.createNodePresentation(actor, Point2D.Float(rect.x, rect.y))
                                Pair(leaf.name, actorPresentation)
                            }
                        }

                        Pair(leaf.name, null)
                    }
                    else -> null
                }
            }.toMap()
            diagram.links.forEach { link ->
                val source = when (link.entity1.name) {
                    "*start*" -> presentationMap["initial"]!!
                    else -> presentationMap[link.entity1.name]
                }
                val target = when (link.entity2.name) {
                    "*end*" -> presentationMap["final"]!!
                    else -> presentationMap[link.entity2.name]
                }
                val transitionLabelRegex =
                    Pattern.compile("""(<?event>\w+)(?:\[(<?guard>\w)])?(?:/(<?action>\w+))?""")
//                diagramEditor.createTransition(source, target)
//                    .also { transition ->
//                        val label = link.label.toString()
//                        val matcher = transitionLabelRegex.matcher(label)
//                        if (transition.label.contains("トリガー")) {
//                            when {
//                                link.label.isWhite -> transition.label = ""
//                                matcher.matches() -> {
//                                    val model = ((transition.model) as ITransition)
//                                    model.event = matcher.group("event") ?: ""
//                                    model.guard = matcher.group("guard") ?: ""
//                                    model.action = matcher.group("action") ?: ""
//                                }
//                                else -> transition.label = label
//                            }
//                        }
//                    }
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        }

        astahDiagram?.let { api.viewManager.diagramViewManager.open(it) }
    }
}