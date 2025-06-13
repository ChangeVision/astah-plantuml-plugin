package com.change_vision.astah.plugins.converter.toastah.presentations.link

import com.change_vision.astah.plugins.converter.toastah.presentations.direction.DirectionInfo
import com.change_vision.astah.plugins.converter.toastah.presentations.direction.DirectionManager
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link

/**
 * リンクプレゼンテーション作成クラス
 */
object LinkPresentationsCreator {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
    
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[LinkCreator] $message")
        }
    }
    
    /**
     * リンクプレゼンテーションを作成する
     * @param linkMap リンクのマッピング
     * @param viewElementMap ノードプレゼンテーションのマップ
     */
    fun createLinkPresentations(
        linkMap: Map<Link, INamedElement>,
        viewElementMap: Map<Entity, INodePresentation>
    ) {
        linkMap.forEach { (link, model) ->
            try {
                val source = viewElementMap[link.entity1] as INodePresentation
                val target = viewElementMap[link.entity2] as INodePresentation
                
                // 関連かどうかをチェックして適切なメソッドを呼び出す
                if (model is IAssociation) {
                    createAssociationPresentation(link, model, source, target)
                } else {
                    // 関連以外のリンクプレゼンテーション
                    diagramEditor.createLinkPresentation(model, source, target)
                }
            } catch (e: Exception) {
                debug("リンク表示の作成に失敗: ${link.entity1?.name} -> ${link.entity2?.name}, 理由: ${e.message}")
            }
        }
    }
    
    /**
     * 関連プレゼンテーションを作成する
     * @param link PlantUMLのリンク
     * @param association Astahの関連
     * @param source ソースノードプレゼンテーション
     * @param target ターゲットノードプレゼンテーション
     */
    private fun createAssociationPresentation(
        link: Link,
        association: IAssociation,
        source: INodePresentation,
        target: INodePresentation
    ) {
        // ラベルの処理
        val rawLabel = if (link.label.isWhite) "" else link.label.toString()
        val labelValue = rawLabel.replace(Regex("\\[|\\]"), "")
        
        // プレゼンテーション作成
        val presentation = diagramEditor.createLinkPresentation(
            association,
            source,
            target
        ) as ILinkPresentation
        
        // 方向情報を設定
        val directionInfo = DirectionManager.getDirectionInfo(link, labelValue)
        DirectionManager.setDirectionProperties(presentation, directionInfo)
    }
} 