package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants

/**
 * 方向に関する変換を行うクラス
 */
object DirectionConverter {
    
    // デバッグログの出力制御フラグ
    private const val DEBUG = false
    
    // デバッグ出力用
    private fun debug(message: String) {
        if (DEBUG) {
            println("[DirectionConverter] $message")
        }
    }
    
    /**
     * 関連のプレゼンテーションから方向矢印を取得する
     * @param association 関連
     * @return 方向を示す矢印文字列
     */
    fun getAssociationDirectionArrow(association: IAssociation): String {
        // 関連のプレゼンテーションを取得
        val presentations = association.getPresentations()
        
        // プレゼンテーションがない場合は方向なし
        if (presentations.isEmpty()) {
            debug("プレゼンテーションが見つかりません: ${association.name}")
            return ""
        }
        
        // LinkPresentationのみをフィルタリング
        val linkPresentations = presentations.filterIsInstance<ILinkPresentation>()
        if (linkPresentations.isEmpty()) {
            debug("リンクプレゼンテーションが見つかりません: ${association.name}")
            return ""
        }
        
        // 最初のプレゼンテーションの方向プロパティを確認
        val firstPresentation = linkPresentations[0]
        
        // 方向表示が有効かどうかを確認
        val isDirectionVisible = firstPresentation.getProperty(
            PresentationPropertyConstants.Key.NAME_DIRECTION_VISIBILITY
        ) == "true"
        
        if (!isDirectionVisible) {
            debug("方向表示が無効: ${association.name}")
            return ""
        }
        
        // 方向の向きを取得
        val directionProperty = firstPresentation.getProperty(
            PresentationPropertyConstants.Key.NAME_DIRECTION_REVERSE
        )
        
        val result = when (directionProperty) {
            PresentationPropertyConstants.Value.NAME_DIRECTION_FORWARD -> ">"
            PresentationPropertyConstants.Value.NAME_DIRECTION_REVERSE -> "<"
            else -> ""
        }
        
        debug("方向矢印: ${association.name} -> $result")
        return result
    }
} 