package com.change_vision.astah.plugins.converter.pattern

/**
 * クラス要素のパターンマッチングを行うクラス
 */
object ClassPattern {

    /**
     * 名前パターンの詳細：
     * - \p{L}: 任意のUnicode文字（日本語、英語、その他言語の文字を含む）
     * - 0-9: 数字
     * - _: アンダースコア
     * - .: ドット
     * - 上記の組み合わせが1回以上繰り返される（[\p{L}0-9_.]+）
     * - または、ダブルクォートで囲まれた任意の文字列（"[^"]+"）
     */
    const val NAME_REGEX = """(?:[\p{L}0-9_.]+|"[^"]+")"""

    // /**
    //  * 型名の制限なし
    //  * シンプルな文字列として扱う
    //  */
    // const val TYPE_REGEX = """.*"""

    // /**
    //  * コロン形式の属性パターン
    //  */
    // const val ATTRIBUTE_COLON_REGEX = """(?i)^(.+?)\\s*:\\s*([\p{L}0-9_.]+|"[^"]+")$"""

    // /**
    //  * 操作パターン
    //  * 名前(パラメータ) の形式を抽出する
    //  */
    // const val OPERATION_REGEX = """(?i)^([\p{L}0-9_.]+|"[^"]+")\\s*\((.*)\)(?:\s*:\s*(.*))?$"""

    /**
     * クラスのステレオタイプを抽出するパターン
     */
    val classStereotypeRegex = Regex("""class\s+($NAME_REGEX)(?:\s*(<<\s*([^>]+?)\s*>>))?""")

    // /**
    //  * 修飾子を抽出するパターン
    //  * 中括弧内のstaticやabstractといった修飾子を抽出
    //  */
    // val modifierPattern = Regex("""\{\s*(static|abstract)(?:\s*,\s*(static|abstract))*\s*\}""")

    /**
     * 中括弧の修飾子パターンを抽出する正規表現
     */
    val curlyBraceRegex = Regex("""\{([^}]*)\}""")
}