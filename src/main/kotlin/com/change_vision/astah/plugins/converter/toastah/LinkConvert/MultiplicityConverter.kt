package com.change_vision.astah.plugins.converter.toastah.LinkConvert

import com.change_vision.jude.api.inf.model.IAttribute
import com.change_vision.jude.api.inf.model.IMultiplicityRange
import com.change_vision.jude.api.inf.exception.InvalidEditingException

/**
 * 多重度変換クラス
 */
object MultiplicityConverter {
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[MultiplicityConverter] $message")
        }
    }
    
    /**
     * 多重度パターン
     */
    private object MultiplicityPatterns {
        // 可視性の正規表現
        val VISIBILITY_PATTERN = Regex("^([+\\-#~])\\s*")
        
        // 多重度の正規表現パターン
        val RANGE_PATTERN = Regex("(\\d+)\\.\\.([*\\d]+)")  // "n..m" or "n..*"
        val SINGLE_VALUE_PATTERN = Regex("^(\\d+)$")         // "n"
        val UNBOUNDED_PATTERN = Regex("^\\*$")               // "*"
        val ONE_TO_MANY_PATTERN = Regex("^1\\.\\*$")         // "1.*"
        val COMMA_SEPARATED_PATTERN = Regex("^([\\d,]+)$")   // "1,3,5,7"
        val COMMA_WITH_RANGE_PATTERN = Regex("^([\\d,]+),(\\d+)\\.\\.(\\d+)$") // "1,3,5,10..100"
    }
    
    /**
     * ラベルから多重度情報を設定する
     * @param memberEnd 関連端
     * @param label ラベル
     */
    fun setMultiplicityFromLabel(memberEnd: IAttribute, label: String) {
        var processedLabel = label.trim()
        debug("ラベル処理開始: '$processedLabel'")
        
        if (processedLabel.isEmpty()) {
            memberEnd.name = ""
            return
        }
        
        // 可視性の処理
        val visibilityString = extractVisibility(processedLabel)
        if (visibilityString != null) {
            try {
                memberEnd.setVisibility(visibilityString)
                debug("可視性設定: $visibilityString")
                
                // 可視性文字を取り除く
                processedLabel = processedLabel.replaceFirst(MultiplicityPatterns.VISIBILITY_PATTERN, "").trim()
                debug("可視性文字除去後: '$processedLabel'")
            } catch (e: InvalidEditingException) {
                debug("可視性設定エラー: ${e.message}")
            }
        }
        
        // 多重度の処理
        val multiplicityResult = extractMultiplicityWithRemainder(processedLabel)
        
        if (multiplicityResult != null) {
            val (ranges, matchedToken, remainingLabel) = multiplicityResult
            
            try {
                memberEnd.setMultiplicity(ranges)
                debug("多重度設定: ${multiplicityRangeToString(ranges)}, マッチした文字列: '$matchedToken'")
                
                // 残ったラベル名を設定
                if (remainingLabel.isNotEmpty()) {
                    memberEnd.name = remainingLabel
                    debug("ラベル名設定: '$remainingLabel'")
                } else {
                    debug("ラベル名なし")
                    memberEnd.name = ""
                }
            } catch (e: InvalidEditingException) {
                debug("多重度設定エラー: ${e.message}")
                when (e.key) {
                    "INVALID_MULTIPLICITY_KEY" -> debug("多重度の値が不正です")
                    "PARAMETER_ERROR_KEY" -> debug("パラメータが不正です")
                    "READ_ONLY_KEY" -> debug("読み取り専用の要素です")
                    else -> debug("その他のエラー: ${e.key}")
                }
                
                // 多重度設定エラーの場合は全体をラベル名に
                memberEnd.name = processedLabel
                debug("多重度設定エラーのため全体をラベル名として設定: '$processedLabel'")
            }
        } else {
            // 多重度が見つからない場合は全体をラベル名とする
            memberEnd.name = processedLabel
            debug("多重度が見つからないため全体をラベル名として設定: '$processedLabel'")
        }
    }
    
    /**
     * 多重度配列を文字列表現に変換（デバッグ用）
     */
    private fun multiplicityRangeToString(range: Array<IntArray>): String {
        return range.joinToString(", ", "[", "]") { 
            val lower = it[0]
            val upper = it[1]
            val upperStr = if (upper == IMultiplicityRange.UNLIMITED) "*" else upper.toString()
            "[$lower..$upperStr]"
        }
    }
    
    /**
     * 可視性を抽出する
     * @param label ラベル
     * @return 可視性文字列、見つからない場合はnull
     */
    private fun extractVisibility(label: String): String? {
        val match = MultiplicityPatterns.VISIBILITY_PATTERN.find(label)
        
        return match?.let {
            val visibilityChar = it.groupValues[1]
            when (visibilityChar) {
                "+" -> "public"
                "-" -> "private"
                "#" -> "protected"
                "~" -> "package"
                else -> null
            }
        }
    }
    
    /**
     * 多重度と残りのラベル名を抽出する
     * @param label ラベル
     * @return Triple(多重度の配列, マッチした文字列, 残りのラベル名)
     */
    private fun extractMultiplicityWithRemainder(label: String): Triple<Array<IntArray>, String, String>? {
        // キーワードに多重度パターンが含まれるか確認
        val tokens = label.split(" ")

        for (index in tokens.indices) {
            val token = tokens[index]
            
            // n..m または n..*
            MultiplicityPatterns.RANGE_PATTERN.find(token)?.let {
                val lowerBound = it.groupValues[1].toInt()
                val upperBound = if (it.groupValues[2] == "*") IMultiplicityRange.UNLIMITED else it.groupValues[2].toInt()
                val ranges = arrayOf(intArrayOf(lowerBound, upperBound))
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
            
            // 単一値 "n"
            MultiplicityPatterns.SINGLE_VALUE_PATTERN.find(token)?.let {
                val value = it.groupValues[1].toInt()
                val ranges = arrayOf(intArrayOf(value, value))
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
            
            // 無制限 "*"
            if (MultiplicityPatterns.UNBOUNDED_PATTERN.matches(token)) {
                val ranges = arrayOf(intArrayOf(IMultiplicityRange.UNLIMITED))
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
            
            // "1..*"
            if (MultiplicityPatterns.ONE_TO_MANY_PATTERN.matches(token)) {
                val ranges = arrayOf(intArrayOf(1, IMultiplicityRange.UNLIMITED))
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
            
            // カンマ区切りの値 "1,3,5,7"
            MultiplicityPatterns.COMMA_SEPARATED_PATTERN.find(token)?.let {
                val values = token.split(",").map { v -> v.toInt() }
                val ranges = Array(values.size) { i -> intArrayOf(values[i], values[i]) }
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
            
            // カンマと範囲の組み合わせ "1,3,5,10..100"
            MultiplicityPatterns.COMMA_WITH_RANGE_PATTERN.find(token)?.let {
                val parts = token.split(",")
                val singleValues = parts.dropLast(1).map { v -> v.toInt() }
                val rangeMatch = MultiplicityPatterns.RANGE_PATTERN.find(parts.last())!!
                val lowerBound = rangeMatch.groupValues[1].toInt()
                val upperBound = if (rangeMatch.groupValues[2] == "*") 
                    IMultiplicityRange.UNLIMITED else rangeMatch.groupValues[2].toInt()
                
                val ranges = Array(singleValues.size + 1) { i ->
                    if (i < singleValues.size) {
                        intArrayOf(singleValues[i], singleValues[i])
                    } else {
                        intArrayOf(lowerBound, upperBound)
                    }
                }
                
                // 残りの文字列を構築
                val remainingTokens = tokens.toMutableList()
                remainingTokens.removeAt(index)
                val remainingLabel = remainingTokens.joinToString(" ")
                
                return Triple(ranges, token, remainingLabel)
            }
        }
        
        return null
    }
    
    /**
     * 多重度を抽出する（下位互換性のため維持）
     * @param label ラベル
     * @return 多重度の配列 [[下限,上限],[下限,上限]...]
     */
    private fun extractMultiplicity(label: String): Array<IntArray>? {
        return extractMultiplicityWithRemainder(label)?.first
    }
}
