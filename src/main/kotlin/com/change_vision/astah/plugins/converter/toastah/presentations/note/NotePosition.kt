package com.change_vision.astah.plugins.converter.toastah.presentations.note

/**
 * ノートの位置を表す列挙型
 */
enum class NotePosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    NONE
}

/**
 * 位置文字列をNotePosition列挙型に変換する
 */
fun parsePosition(positionStr: String): NotePosition {
    return when (positionStr.toLowerCase()) {
        "top" -> NotePosition.TOP
        "bottom" -> NotePosition.BOTTOM
        "left" -> NotePosition.LEFT
        "right" -> NotePosition.RIGHT
        else -> NotePosition.RIGHT
    }
} 