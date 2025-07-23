package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IUseCase
import com.change_vision.jude.api.inf.model.IUseCaseDiagram
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.abel.LeafType.USECASE
import net.sourceforge.plantuml.abel.LeafType.USECASE_BUSINESS
import net.sourceforge.plantuml.abel.LeafType.DESCRIPTION
import net.sourceforge.plantuml.abel.LinkArrow
import net.sourceforge.plantuml.decoration.LinkDecor
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

object ToAstahUseCaseDiagramConverter {
    private val DEFAULT_RECT = Rectangle2D.Float(30f, 30f, 30f, 30f)

    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.useCaseDiagramEditor
    private val modelEditor = projectAccessor.modelEditorFactory.useCaseModelEditor
    private val basicModelEditor = projectAccessor.modelEditorFactory.basicModelEditor

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
            val project = projectAccessor.project
            val presentationMap = diagram.leafs().mapNotNull { leaf ->
                val posKey = if (leaf.display.size() > 0) leaf.display.first()
                             else ""
                val rect = posMap.getOrDefault(posKey, DEFAULT_RECT)

                val model: INamedElement? = when (leaf.leafType) {
                    // ユースケース
                    USECASE -> {
                        val modelName = leaf.display.joinToString(separator = "\n") { it }
                        modelEditor.createUseCase(project, modelName)
                    }
                    USECASE_BUSINESS -> {
                        // ビジネスユースケース
                        val modelName = leaf.display.joinToString(separator = "\n") { it }
                        val model = modelEditor.createUseCase(project, modelName)
                        model.addStereotype("business")
                        model
                    }
                    DESCRIPTION -> {
                        val modelName = leaf.display.joinToString(separator = " ") { it }
                        val symbolStyle = leaf.uSymbol.sNames
                        if (!symbolStyle.isNullOrEmpty()) {
                            when (leaf.uSymbol.sNames[0].name) {
                                "actor" -> modelEditor.createActor(project, modelName) as INamedElement // アクター
                                "business" -> { // ビジネスアクター
                                    val actor = modelEditor.createActor(project, modelName)
                                    actor.addStereotype("business")
                                    actor
                                }
                                else -> null
                            }
                        } else {
                            null
                        }
                    }
                    else -> null
                }

                if (model == null) {
                    Pair(leaf.name, null)
                } else {
                    val presentation = diagramEditor.createNodePresentation(model, Point2D.Float(rect.x, rect.y))
                    Pair(leaf.name, presentation)
                }
            }.toMap()

            // 関係線(関連、拡張、包含)の作成
            val regex = """<?-(left|right|up|down|-*)-?>?""".toRegex()
            val sources = reader.blocks.firstOrNull()?.data

            diagram.links.filter { !it.type.style.isInvisible }.forEach { link ->
                val entity1 = presentationMap[link.entity1.name]
                val entity2 = presentationMap[link.entity2.name]
                if (entity1 == null || entity2 == null) {
                    return@forEach
                }

                val code = sources?.getOrNull(link.location.position)?.toString().orEmpty()
                val matchResult = regex.find(code,0)


                // 関連の方向指定をパース
                val direction = matchResult?.value.orEmpty()
                val isReverse = when {
                    direction.contains("le") || direction.contains("up") -> true
                    direction.contains("ri") || direction.contains("do") -> false
                    else -> false // デフォルト（方向指定がない場合）
                }

                val (source, target) = if (isReverse) {
                    entity2 to entity1
                } else {
                    entity1 to entity2
                }

                val linkPresentation = if (link.label.isWhite) {
                    val model = basicModelEditor.createAssociation(
                        (source?.model as IClass),
                        (target?.model as IClass),
                        null,
                        null,
                        null)
                    diagramEditor.createLinkPresentation(model, source, target)
                } else {
                    when (val linkLabel = link.label.get(0)) {
                        "include" -> {
                            val model = modelEditor.createInclude(
                                (target?.model as IUseCase),
                                (source?.model as IUseCase),
                                ""
                            )
                            diagramEditor.createLinkPresentation(model, target, source)
                        }
                        "extends" -> {
                            val model = modelEditor.createExtend(
                                (target?.model as IUseCase),
                                (source?.model as IUseCase),
                                ""
                            )
                            diagramEditor.createLinkPresentation(model, target, source)
                        }
                        else -> {
                            if (link.linkArrow == LinkArrow.NONE_OR_SEVERAL && link.type.decor1 == LinkDecor.EXTENDS) {
                                val model = basicModelEditor.createGeneralization(
                                    (source?.model as IClass),
                                    (target?.model as IClass),
                                    linkLabel?.toString()
                                )
                                diagramEditor.createLinkPresentation(model, source, target)
                            } else {
                                val model = basicModelEditor.createAssociation(
                                    (source?.model as IClass),
                                    (target?.model as IClass),
                                    linkLabel?.toString(),
                                    null,
                                    null
                                )
                                diagramEditor.createLinkPresentation(model, source, target)
                            }
                        }
                    }
                }
                (linkPresentation.model as? IAssociation)?.let { assoc ->
                    if(matchResult?.value.toString().indexOf(">") >= 0 ){
                        assoc.memberEnds[1].navigability = "Navigable"
                    }
                    if(matchResult?.value.toString().indexOf("<") >= 0 ){
                        assoc.memberEnds[0].navigability = "Navigable"
                    }
                }
            }
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            TransactionManager.abortTransaction()
        } catch (e: Exception) {
            if (TransactionManager.isInTransaction()) {
                TransactionManager.abortTransaction()
            }
        }

        astahDiagram?.let { api.viewManager.diagramViewManager.open(it) }
    }
}