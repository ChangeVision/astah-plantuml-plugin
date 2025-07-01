package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IEnumeration
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IParameter
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants
import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.model.IUseCase

/**
 * クラス図のクラス要素を変換するクラス
 */
object ClassConverter {

    private const val DEBUG = false

    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[ClassConverter] $message")
        }
    }

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz クラス
     * @param sb 出力用のStringBuilder
     */
    fun convert(clazz: IClass, sb: StringBuilder) {
        // 1) 型判定:enum / interface / abstract / class
        var type = when {
            clazz is IEnumeration -> "enum"
            clazz.hasStereotype("interface") -> determineInterfaceNotation(clazz)
            clazz.isAbstract -> "abstract"
            else -> "class"
        }

        sb.append("$type ${formatName(clazz.name)}")

        // 2) ステレオタイプから 'interface', 'enumeration' を除外する
        val stereotypes = clazz.stereotypes.filterNot {
            it.equals("interface", ignoreCase = true) ||
            it.equals("enumeration", ignoreCase = true)
        }

        // 残ったステレオタイプだけを <<>> で出力
        if (stereotypes.isNotEmpty()) {
            sb.append(" <<")
            sb.append(stereotypes.joinToString(","))
            sb.append(">>")
        }

        val fields = clazz.attributes.filter { it.association == null }
        if (fields.isNotEmpty() || clazz.operations.isNotEmpty()) {
            sb.appendLine("{")
            fields.forEach { field ->
                sb.append("  ")
                val modifiers = mutableListOf<String>()
                if (field.isStatic) modifiers.add("static")
                if (field.hasStereotype("abstract")) modifiers.add("abstract")
                if (modifiers.isNotEmpty()) {
                    sb.append("{${modifiers.joinToString(",")}} ")
                }
                sb.appendLine(visibility(field) + field.name + " : " + field.type.name)
            }
            clazz.operations.forEach { op ->
                sb.append("  ")
                val modifiers = mutableListOf<String>()
                if (op.isStatic) modifiers.add("static")
                if (op.isAbstract) modifiers.add("abstract")
                if (modifiers.isNotEmpty()) {
                    sb.append("{${modifiers.joinToString(",")}} ")
                }
                sb.appendLine(visibility(op) + op.name + "(" + params(op.parameters) + ") : " + op.returnType.name)
            }
            sb.append("}")
        }
        sb.appendLine()
    }

    /**
     * インターフェースの表記方法を判定する
     * @param clazz インターフェースクラス
     * @return 表記タイプ（"interface"または"circle"）
     */
    private fun determineInterfaceNotation(clazz: IClass): String {
        // クラスのプレゼンテーションを取得
        val presentations = clazz.getPresentations()

        if (presentations.isEmpty()) {
            debug("プレゼンテーションが見つかりません: ${clazz.name}")
            return "interface"
        }

        for (pres in presentations) {
            // プレゼンテーションから表記タイプを取得
            val notationType = pres.getProperty(PresentationPropertyConstants.Key.NOTATION_TYPE)
            debug("インターフェース ${clazz.name} の表記タイプ: $notationType")

            // アイコン表記の場合はcircleとして表示（DESCRIPTIONとCIRCLE両方）
            if (notationType == PresentationPropertyConstants.Value.NOTATION_TYPE_ICON) {
                return "circle"
            }
        }

        return "interface"
    }

    /**
     * 要素の可視性を表す記号を取得する
     * @param element 名前付き要素
     * @return 可視性を表す記号 (+, -, #, ~)
     */
    fun visibility(element: INamedElement): String =
        when {
            element.isPrivateVisibility -> "-"
            element.isProtectedVisibility -> "#"
            element.isPackageVisibility -> "~"
            element.isPublicVisibility -> "+"
            else -> ""
        }

    /**
     * パラメータリストを文字列に変換
     * @param params パラメータの配列
     * @return パラメータの文字列表現
     */
    private fun params(params: Array<IParameter>): String =
        params.map { it.name + ":" + it.type.name }.joinToString(", ")

    /**
     * 名前をPlantUML形式にフォーマットする
     * @param namedElement エレメント
     * @return フォーマットされた名前
     */
    fun formatName(namedElement: INamedElement): String {
        return  when(namedElement){
            is IClass -> formatName(namedElement)
            else -> formatName(namedElement.name)
        }
    }

    /**
     * 名前をPlantUML形式にフォーマットする
     * @param clazz クラスオブジェクト
     * @return フォーマットされた名前
     */
    fun formatName(clazz: IClass): String {
        //前処理、後処理しながらformatNameに渡す。
        if(clazz !is IUseCase && !clazz.hasStereotype("actor")) return formatName(clazz.name)
        var  result : String = ""

        result = when{
            clazz is IUseCase -> "(${formatName(clazz.name)})"

            clazz.hasStereotype("actor") -> ":${formatName(clazz.name)}:"
            else -> formatName(clazz.name)
        }

        if(clazz.hasStereotype("business")) result += "/"

        return result
    }

    /**
     * 名前をPlantUML形式にフォーマットする
     * @param name 名前
     * @return フォーマットされた名前
     */
    fun formatName(name: String): String {
        // 正規表現パターンに一致する単純な名前の場合はそのまま返し、
        // それ以外（空白やスラッシュなどの特殊文字を含む場合）はダブルクォーテーションで囲む
        val nameRegex = Regex("^${ClassPattern.NAME_REGEX}\$")
        // 単純な名前はそのまま返す
        if (nameRegex.matches(name) && !name.contains("\"")) {
            return name
        }
        // ダブルクォートで囲む場合、先頭に#や*があればチルダを追加
        else {
            val escapedName = name.replace(Regex("^([#*])"), "~$1")
            return "\"${escapedName.replace("\n", "\\n")}\""
        }
    }
}