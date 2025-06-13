package com.change_vision.astah.plugins.converter.toastah.classbody

/**
 * 可視性の変換を行うクラス
 */
object VisibilityConverter {

    /**
     * 可視性記号をUMLの可視性に変換する
     * @param symbol 可視性記号（+、-、#、~）
     * @return UMLの可視性文字列（public、private、protected、package）
     */
    fun mapVisibility(symbol: String): String {
        return when (symbol) {
            "+" -> "public"
            "-" -> "private"
            "#" -> "protected"
            "~" -> "package"
            "public", "private", "protected", "package" -> symbol
            else -> "package"
        }
    }

    /**
     * テキストから可視性記号を除去する
     * @param text 対象のテキスト
     * @return 可視性記号を除去したテキスト
     */
    fun removeVisibility(text: String): String {
        return if (text.isNotEmpty() && text.first() in listOf('+', '-', '#', '~')) {
            text.substring(1).trim()
        } else {
            text.trim()
        }
    }
}
