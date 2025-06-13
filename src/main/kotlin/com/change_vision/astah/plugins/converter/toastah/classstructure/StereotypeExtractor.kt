package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.pattern.ClassPattern

/**
 * ステレオタイプの抽出を行うクラス
 */
object StereotypeExtractor {

    /**
     * PlantUMLテキストからステレオタイプを抽出する
     * @param text PlantUMLのテキスト
     * @return クラス名とステレオタイプのマッピング
     */
    fun extract(text: String): Result<Map<String, List<String>>> {
        return try {
            val result = mutableMapOf<String, List<String>>()
            
            // クラスのステレオタイプを抽出
            ClassPattern.classStereotypeRegex.findAll(text).forEach { matchResult ->
                val classNameStr = matchResult.groupValues[1]
                // <<>>を含まないステレオタイプ名部分を取得（正規表現の3番目のグループ）
                val stereotypeName = matchResult.groupValues[3]
                
                if (stereotypeName.isNotEmpty()) {
                    // ステレオタイプを分割して抽出（カンマで区切られている場合）
                    val stereotypes = stereotypeName
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    if (stereotypes.isNotEmpty()) {
                        result[classNameStr] = stereotypes
                    }
                }
            }
            
            Result.success(result.toMap())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
