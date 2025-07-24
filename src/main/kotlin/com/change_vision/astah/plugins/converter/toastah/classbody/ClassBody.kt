package com.change_vision.astah.plugins.converter.toastah.classbody

import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.model.IClass

/**
 * クラス本体の変換を行うクラス
 */
object ClassBody {
    /**
     * クラス本体を解析して属性と操作を生成する
     * @param aClass 対象のクラス
     * @param bodyTexts クラス本体のテキスト行リスト
     */
    fun createClassBody(aClass: IClass, bodyTexts: List<CharSequence>) {
        for (text in bodyTexts) {
            var line = text.toString().trim()
            if (line.isEmpty()) continue
            
            try {
                // 修飾子の抽出
                val modifiers = ModifiersConverter.extractModifiers(line)
    
                // 中括弧の除去[]
                line = removeCurlyBraces(line)
    
                // 操作または属性の判定と処理
                if (line.contains("(") && line.contains(")")) {
                    OperationConverter.processOperation(line, aClass, modifiers)
                } else {
                    AttributeConverter.processAttribute(line, aClass, modifiers)
                }
            } catch (e: Exception) {
                // ここでエラーとなった行の処理はスキップし、詳細ログを出力
                println("要素変換に失敗: ${e.message} -> 行: $line")
            }
        }
    }

    /**
     * 中括弧を除去する
     * @param line 対象の行テキスト
     * @return 中括弧を除去したテキスト
     */
    private fun removeCurlyBraces(line: String): String {
        return ClassPattern.curlyBraceRegex.replace(line, "").trimStart()
    }
}
