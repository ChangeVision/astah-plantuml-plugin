package com.change_vision.astah.plugins.converter.toastah.presentations.direction

import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor

enum class DirectionInfo {
    NONE,    // 方向指定なし
    FORWARD, // 順方向（>）
    REVERSE  // 逆方向（<）
}
object Constants {
    val NAME_DIRECTION_VISIBILITY = PresentationPropertyConstants.Key.NAME_DIRECTION_VISIBILITY
    val NAME_DIRECTION_REVERSE = PresentationPropertyConstants.Key.NAME_DIRECTION_REVERSE
    val NAME_DIRECTION_FORWARD = PresentationPropertyConstants.Value.NAME_DIRECTION_FORWARD
    val NAME_DIRECTION_REVERSE_VALUE = PresentationPropertyConstants.Value.NAME_DIRECTION_REVERSE
} 

/**
 * 方向情報を管理するクラス
 */
object DirectionManager {
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[DirectionManager] $message")
        }
    }
    
    /**
     * リンクの方向情報を取得する
     * @param link PlantUMLのリンク
     * @param labelValue ラベル値
     * @return 方向情報
     */
    fun getDirectionInfo(link: Link, labelValue: String): DirectionInfo {
        debug("リンク方向処理: ${link.entity1?.name} -> ${link.entity2?.name}")
        
        // 1)  ARROW decorator を確認
        if (link.type.decor2 == LinkDecor.ARROW) {
            debug("ARROW装飾子(decor2)が見つかりました: 順方向")
            return DirectionInfo.FORWARD   // --> の「先頭側」に矢印
        }
        if (link.type.decor1 == LinkDecor.ARROW) {
            debug("ARROW装飾子(decor1)が見つかりました: 逆方向")
            return DirectionInfo.REVERSE   // <-- の「後尾側」に矢印
        }

        // 2) linkArrowで確認
        debug("linkArrow: ${link.linkArrow?.name}")
        when (link.linkArrow?.name) {
            "DIRECT_NORMAL" -> {
                debug("DIRECT_NORMAL: 順方向")
                return DirectionInfo.FORWARD
            }
            "BACKWARD" -> {
                debug("BACKWARD: 逆方向")
                return DirectionInfo.REVERSE
            }
        }

        // 3) ラベル文字列で確認
        debug("Arrow不明のため、ラベルから方向を判定: '$labelValue'")
        val result = when {
            labelValue.endsWith(">")      -> DirectionInfo.FORWARD
            labelValue.contains(" > ")    -> DirectionInfo.FORWARD
            labelValue.endsWith("<")      -> DirectionInfo.REVERSE
            labelValue.contains(" < ")    -> DirectionInfo.REVERSE
            else                          -> DirectionInfo.NONE
        }
        
        debug("ラベルからの判定結果: $result")
        return result
    }
    
    /**
     * リンクプレゼンテーションに方向プロパティを設定する
     * @param presentation リンクプレゼンテーション
     * @param directionInfo 方向情報
     */
    fun setDirectionProperties(presentation: ILinkPresentation, directionInfo: DirectionInfo) {
        debug("方向プロパティの設定: $directionInfo")
        
        when (directionInfo) {
            DirectionInfo.FORWARD -> {
                // 順方向（>）の場合
                debug("順方向の設定")
                presentation.setProperty(
                    Constants.NAME_DIRECTION_VISIBILITY,
                    "true"
                )
                presentation.setProperty(
                    Constants.NAME_DIRECTION_REVERSE,
                    Constants.NAME_DIRECTION_FORWARD
                )
            }
            DirectionInfo.REVERSE -> {
                // 逆方向（<）の場合
                debug("逆方向の設定")
                presentation.setProperty(
                    Constants.NAME_DIRECTION_VISIBILITY,
                    "true"
                )
                presentation.setProperty(
                    Constants.NAME_DIRECTION_REVERSE,
                    Constants.NAME_DIRECTION_REVERSE_VALUE
                )
            }
            DirectionInfo.NONE -> {
                // 方向指定なしの場合
                debug("方向指定なしの設定")
                presentation.setProperty(
                    Constants.NAME_DIRECTION_VISIBILITY,
                    "false"
                )
            }
        }
    }
    
} 