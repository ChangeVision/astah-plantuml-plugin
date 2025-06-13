package com.change_vision.astah.plugins.converter.toastah.classbody

import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.model.IAttribute
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IOperation

/**
 * 修飾子の変換を行うクラス
 */
object ModifiersConverter {

    /**
     * 修飾子情報を保持するデータクラス
     * @property isStatic 静的修飾子
     * @property isAbstract 抽象修飾子
     */
    data class Modifiers(
        var isStatic: Boolean = false,
        var isAbstract: Boolean = false
    )

    /**
     * 行テキストから修飾子を抽出する
     * @param line 対象の行テキスト
     * @return 抽出された修飾子情報
     */
    fun extractModifiers(line: String): Modifiers {
        val result = Modifiers()
        ClassPattern.curlyBraceRegex.findAll(line).forEach { match ->
            val content = match.groupValues[1].lowercase()
            if ("static" in content) result.isStatic = true
            if ("abstract" in content) result.isAbstract = true
        }
        return result
    }

    /**
     * 属性に修飾子を適用する
     * @param attr 対象の属性
     * @param modifiers 適用する修飾子情報
     */
    fun applyModifiersForAttribute(attr: IAttribute, modifiers: Modifiers) {
        if (modifiers.isStatic) attr.isStatic = true
    }

    /**
     * 操作に修飾子を適用する
     * @param op 対象の操作
     * @param modifiers 適用する修飾子情報
     */
    fun applyModifiersForOperation(op: IOperation, modifiers: Modifiers) {
        if (modifiers.isAbstract) op.isAbstract = true
        if (modifiers.isStatic) op.isStatic = true
    }
}
