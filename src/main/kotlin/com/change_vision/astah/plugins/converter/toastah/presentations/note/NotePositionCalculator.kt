package com.change_vision.astah.plugins.converter.toastah.presentations.note

import com.change_vision.jude.api.inf.presentation.INodePresentation
import java.awt.geom.Point2D

/**
 * ノート位置を計算するクラス
 */
object NotePositionCalculator {
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[NotePositionCalculator] $message")
        }
    }
    
    /**
     * クラスプレゼンテーションとノート位置指定に基づいてノートの位置を計算する
     * @param classPresentation 関連するクラスのプレゼンテーション
     * @param position ノート位置（TOP、BOTTOM、LEFT、RIGHT）
     * @param offsetX X方向のオフセット
     * @param offsetY Y方向のオフセット
     * @return ノートの配置座標
     */
    fun calculateNotePosition(
        classPresentation: INodePresentation,
        position: NotePosition,
        offsetX: Double,
        offsetY: Double
    ): Point2D.Double {
        val rect = classPresentation.rectangle
        val margin = 20.0
        
        return when (position) {
            NotePosition.TOP -> Point2D.Double(
                rect.centerX - offsetX / 2,
                rect.minY - offsetY - margin
            )
            NotePosition.BOTTOM -> Point2D.Double(
                rect.centerX - offsetX / 2,
                rect.maxY + margin
            )
            NotePosition.LEFT -> Point2D.Double(
                rect.minX - offsetX - margin,
                rect.centerY - offsetY / 2
            )
            NotePosition.RIGHT -> Point2D.Double(
                rect.maxX + margin,
                rect.centerY - offsetY / 2
            )
            NotePosition.NONE -> Point2D.Double(
                rect.maxX + offsetX + margin,
                rect.maxY + offsetY + margin
            )
        }
    }
} 