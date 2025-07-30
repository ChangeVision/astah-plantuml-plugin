package com.change_vision.astah.plugins.converter.toastah.presentations.node

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.presentation.INodePresentation
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.LeafType
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

/**
 * ノードプレゼンテーション作成クラス
 */
object NodePresentationsCreator {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
    
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[NodeCreator] $message")
        }
    }

    /**
     * ノードプレゼンテーションを作成する
     * @param entityMap エンティティのマッピング
     * @param positionMap 位置情報のマッピング
     * @return 作成されたノードプレゼンテーションのマップ
     */
    fun createNodePresentations(
        entityMap: Map<Entity, IClass>,
        positionMap: Map<String, Rectangle2D.Float>
    ): Map<Entity, INodePresentation> {
        return entityMap.keys
            .filter { isStandardNotationType(it) }  // 標準表記で表示する要素だけをフィルタリング
            .mapNotNull { entity ->
                val code = "class " + entity.name
                if (positionMap.containsKey(code)) {
                    val position = positionMap[code]!!
                    // ノードプレゼンテーションの作成
                    val viewElement = diagramEditor.createNodePresentation(
                        entityMap[entity],
                        Point2D.Float(position.x, position.y)
                    ) as INodePresentation
                    
                    // 通常クラスは標準表記に設定
                    viewElement.setProperty(
                        PresentationPropertyConstants.Key.NOTATION_TYPE,
                        PresentationPropertyConstants.Value.NOTATION_TYPE_NORMAL
                    )
                    
                    // as ""の表記を図上のラベルに表示する
                    val displayName = entity.display.toString().replace(Regex("\\[|\\]"), "")
                    viewElement.setLabel(displayName)

                    Pair(entity, viewElement)
                } else {
                    debug("表示位置が見つかりません: ${entity.name}")
                    null
                }
            }.toMap()
    }
    
    /**
     * 標準表記で表示する要素かどうかを判定する
     * クラスおよび通常のインターフェース(INTERFACE)を処理する
     */
    private fun isStandardNotationType(entity: Entity): Boolean {
        // アイコン表記のインターフェース(CIRCLE, DESCRIPTION)はInterfacePresentationsCreatorで処理するため除外
        return entity.leafType != LeafType.CIRCLE && entity.leafType != LeafType.DESCRIPTION
    }
} 