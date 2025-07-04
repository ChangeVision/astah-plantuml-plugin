package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IAttribute
import com.change_vision.jude.api.inf.model.IMultiplicityRange

/**
 * 多重度変換ユーティリティクラス
 * 多重度情報をPlantUML表記に変換するためのメソッドを提供します
 */
object MultiplicityConverter {
    
    // デバッグログの出力制御フラグ
    private const val DEBUG = false
    
    // デバッグ出力用
    private fun debug(message: String) {
        if (DEBUG) {
            println("[MultiplicityConverter] $message")
        }
    }
    
    /**
     * 属性から多重度を取得し、文字列に変換する
     * @param attr 属性 (関連エンド)
     * @return 多重度の文字列表現（例: "0..*", "1"）
     */
    fun getMultiplicityString(attr: IAttribute): String {
        try {
            debug("多重度文字列の取得開始: 属性名=${attr.name}, 型=${attr.type.name}")
            
            // 多重度の情報をデバッグ出力
            val multiplicityRanges = attr.multiplicity
            debugMultiplicityRanges(multiplicityRanges)
            
            // 可視性と名前を取得
            val visibility = ClassConverter.visibility(attr)
            val name = attr.name.orEmpty()
            
            debug("可視性: '$visibility', 名前: '$name'")
            
            // 多重度文字列を生成
            val multiplicityStr = convertMultiplicityRangesToString(multiplicityRanges)
            
            // 結果を組み立てる
            val result = buildResultString(visibility, name, multiplicityStr)
            
            debug("最終結果: '$result'")
            return result
        } catch (e: Exception) {
            // エラーが発生した場合は詳細を出力
            debug("多重度の取得中にエラー発生: ${e.message}")
            e.printStackTrace()
            return ""
        }
    }
    
    /**
     * 多重度配列の内容をデバッグ出力する
     */
    private fun debugMultiplicityRanges(multiplicityRanges: Array<IMultiplicityRange>) {
        debug("多重度配列の長さ: ${multiplicityRanges.size}")
        multiplicityRanges.forEachIndexed { index, range ->
            val lower = range.getLower()
            val upper = range.getUpper()
            debug("多重度[$index]: 下限=$lower, 上限=${if (upper == IMultiplicityRange.UNLIMITED) "無制限" else upper}")
        }
    }
    
    /**
     * 多重度配列を文字列表現に変換する
     */
    private fun convertMultiplicityRangesToString(multiplicityRanges: Array<IMultiplicityRange>): String {
        if (multiplicityRanges.isEmpty()) {
            debug("多重度が設定されていません")
            return ""
        }
        
        val rangeStrings = mutableListOf<String>()
        
        for (range in multiplicityRanges) {
            val lower = range.getLower()
            val upper = range.getUpper()
            
            debug("多重度処理中: 下限=$lower, 上限=${if (upper == IMultiplicityRange.UNLIMITED) "無制限" else upper}")
            
            // -100は未定義値(IMultiplicityRange.UNDEFINED)なので表示しない
            if (lower == -100 || (upper != IMultiplicityRange.UNLIMITED && upper == -100)) {
                debug("未定義値を検出: スキップします")
                continue
            }
            
            // 多重度文字列を生成
            val rangeStr = formatMultiplicityRange(lower, upper)
            rangeStrings.add(rangeStr)
        }
        
        val result = rangeStrings.joinToString(",")
        debug("最終的な多重度文字列: '$result'")
        return result
    }
    
    /**
     * 多重度範囲を文字列形式に変換する
     */
    private fun formatMultiplicityRange(lower: Int, upper: Int): String {
        return when {
            // 「*」の場合（下限が-1で上限が無制限の場合）
            upper == IMultiplicityRange.UNLIMITED && lower == -1 -> {
                debug("無制限(-1..無制限)を検出: '*'として表示")
                "*"
            }
            // 「0..*」の場合（下限が0で上限が無制限の場合）
            upper == IMultiplicityRange.UNLIMITED && lower == 0 -> {
                debug("無制限範囲(0..*)を検出: '0..*'として表示")
                "0..*"
            }
            // その他の「n..*」の場合
            upper == IMultiplicityRange.UNLIMITED -> {
                debug("範囲文字列を生成: '$lower..*'")
                "$lower..*"
            }
            // 単一値の場合
            upper == lower -> {
                debug("単一値を検出: '$lower'")
                lower.toString()
            }
            // 範囲指定の場合
            else -> {
                debug("範囲を検出: '$lower..$upper'")
                "$lower..$upper"
            }
        }
    }
    
    /**
     * 可視性、名前、多重度を組み合わせた結果文字列を生成する
     */
    private fun buildResultString(visibility: String, name: String, multiplicityStr: String): String {
        val result = StringBuilder()
        
        // 名前が設定されていれば、可視性と名前を追加
        if (name.isNotEmpty()) {
            if (visibility.isNotEmpty()) {
                result.append(visibility)
                result.append(" ")
                debug("可視性を追加: '$visibility'")
            }

            result.append(name)
            debug("名前を追加: '$name'")
        }
        
        // 多重度が設定されていれば追加（名前または可視性との間にスペースを入れる）
        if (multiplicityStr.isNotEmpty()) {
            if (result.isNotEmpty()) result.append(" ")
            result.append(multiplicityStr)
            debug("多重度を追加: '$multiplicityStr'")
        }
        
        return result.toString()
    }
    
    /**
     * ダブルクォート直後の#や*の前にチルダを追加する
     * @param multiplicityText 多重度テキスト
     * @return エスケープ処理された多重度テキスト
     */
    fun escapeMultiplicityText(multiplicityText: String): String {
        return if (multiplicityText.startsWith("#") || multiplicityText.startsWith("*")) {
            "~" + multiplicityText
        } else {
            multiplicityText
        }
    }
} 