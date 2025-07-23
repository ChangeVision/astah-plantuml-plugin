package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IParameter
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants

interface IClassConverter {
    private val defaultHiddenStereotypes : List<String> get() = listOf("interface","enumeration")
    // 追加で変換から除外したいステレオタイプがある場合、継承先でオーバーライドしてください。
    val customHiddenStereotypes : List<String> get() = listOf()

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz クラス
     * @param sb 出力用のStringBuilder
     */
    fun convert(clazz: IClass, sb: StringBuilder)

    fun IClassConverter.convertStereotype(stereotypes : List<String>) : String{
        return " ${stereotypes.joinToString(separator = " ") { "<<$it>>" }}"
    }

    private fun filterStereotypes(stereotypes : List<String>, optionalBlackList : List<String> = emptyList()) : List<String>{
        return stereotypes.filterNot { it in optionalBlackList }
    }

    fun IClassConverter.filterStereotypes(clazz : IClass , optionalBlackList : List<String> = emptyList()) : List<String>{
        return filterStereotypes(clazz.stereotypes.toList() , optionalBlackList)
    }

    // IClassの種類を設定するステレオタイプを除外する
    fun IClassConverter.filterStereotypesForClass(clazz : IClass , optionalBlackList : List<String> = emptyList()) : List<String>{
        return filterStereotypes(clazz.stereotypes
            .filterIndexed { index, stereotype -> !(index == 0 && (stereotype in defaultHiddenStereotypes + customHiddenStereotypes)) } ,
            optionalBlackList)
    }

    fun isValidClass(clazz : IClass) : Boolean{
        return true
    }

    /**
     * インターフェースの表記方法を判定する
     * @param clazz インターフェースクラス
     * @return 表記タイプ（"interface"または"circle"）
     */
    fun IClassConverter.determineInterfaceNotation(clazz: IClass): String {
        // クラスのプレゼンテーションを取得
        val presentations = clazz.presentations

        if (presentations.isEmpty()) {
            return "interface"
        }

        for (pres in presentations) {
            // プレゼンテーションから表記タイプを取得
            val notationType = pres.getProperty(PresentationPropertyConstants.Key.NOTATION_TYPE)

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
    fun IClassConverter.params(params: Array<IParameter>): String =
        params.joinToString(", ") { it.name + ":" + it.type.name }

    /**
     * 名前をPlantUML形式にフォーマットする
     * @param namedElement エレメント
     * @return フォーマットされた名前
     */
    fun formatName(namedElement: INamedElement): String {
        return when(namedElement){
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
        return formatName(clazz.name)
    }

    /**
     * 名前をPlantUML形式にフォーマットする
     * @param name 名前
     * @return フォーマットされた名前
     */
    fun formatName(name: String): String {
        // 正規表現パターンに一致する単純な名前の場合はそのまま返し、
        // それ以外（空白やスラッシュなどの特殊文字を含む場合）はダブルクォーテーションで囲む
        val nameRegex = Regex("^${ClassPattern.NAME_REGEX}$")
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