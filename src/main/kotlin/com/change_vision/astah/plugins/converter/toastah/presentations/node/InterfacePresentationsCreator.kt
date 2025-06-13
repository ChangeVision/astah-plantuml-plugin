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
 * インターフェース用ノードプレゼンテーション作成クラス
 */
object InterfacePresentationsCreator {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
    
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[InterfaceCreator] $message")
        }
    }

    /**
     * インターフェース用のノードプレゼンテーションを作成する
     * @param entityMap エンティティのマッピング
     * @param positionMap 位置情報のマッピング
     * @return 作成されたノードプレゼンテーションのマップ
     */
    fun createInterfacePresentations(
        entityMap: Map<Entity, IClass>,
        positionMap: Map<String, Rectangle2D.Float>
    ): Map<Entity, INodePresentation> {
        return entityMap.keys
            .filter { isInterfaceType(it) }
            .mapNotNull { entity ->
                // まずはプレフィックスなしで検索、見つからなければプレフィックス付きで検索
                val entityCode = entity.name//要動作確認
                val prefixedCode = "class " + entityCode
                val position = when {
                    positionMap.containsKey(entityCode) -> positionMap[entityCode]
                    positionMap.containsKey(prefixedCode) -> positionMap[prefixedCode]
                    else -> null
                }
                
                if (position != null) {
                    // ノードプレゼンテーションの作成
                    val viewElement = diagramEditor.createNodePresentation(
                        entityMap[entity],
                        Point2D.Float(position.x, position.y)
                    ) as INodePresentation
                    
                    // インターフェースはアイコン表記に設定
                    viewElement.setProperty(
                        PresentationPropertyConstants.Key.NOTATION_TYPE,
                        PresentationPropertyConstants.Value.NOTATION_TYPE_ICON
                    )
                    
                    // as ""の表記を図上のラベルに表示する
                    val displayName = entity.display.toString().replace(Regex("\\[|\\]"), "")
                    viewElement.setLabel(displayName)

                    Pair(entity, viewElement)
                } else {
                    debug("表示位置が見つかりません: ${entity.name}")//要動作確認
                    null
                }
            }.toMap()
    }
    
    /**
     * インターフェースタイプかどうかを判定する
     */
    private fun isInterfaceType(entity: Entity): Boolean {
        // インターフェース表記のcircleとdescriptionのみを処理する
        return entity.leafType == LeafType.CIRCLE || 
               entity.leafType == LeafType.DESCRIPTION
    }
} 