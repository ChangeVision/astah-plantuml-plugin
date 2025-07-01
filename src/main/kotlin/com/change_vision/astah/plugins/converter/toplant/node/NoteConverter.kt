package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.astah.plugins.converter.toastah.presentations.note.NotePosition

interface NoteConverter {
    /**
     * 位置情報を文字列に変換する
     */
    fun positionToString(position: NotePosition): String {
        return when (position) {
            NotePosition.TOP -> "top"
            NotePosition.BOTTOM -> "bottom"
            NotePosition.LEFT -> "left"
            NotePosition.RIGHT -> "right"
            NotePosition.NONE -> "right" // NONEの場合はrightとして扱う
        }
    }

    /**
     * テキストをPlantUML用にエスケープ
     */
    fun escape(text: String) = text.replace("\n", "\\n")
}