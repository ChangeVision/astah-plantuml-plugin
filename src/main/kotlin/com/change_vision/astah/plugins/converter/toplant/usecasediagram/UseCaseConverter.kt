package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.node.IClassConverter
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IUseCase

object UseCaseConverter : IClassConverter {
    //toPlantの際、変換しないステレオタイプ
    val excludeStereotypes = setOf("interface", "enumeration", "entity", "boundary", "control")
    //変換はするが記載しないステレオタイプ
    override val customHiddenStereotypes : List<String> get() = listOf("actor")
    private const val STEREOTYPE_BUSINESS = "business"

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz ユースケースかアクター
     * @param sb 出力用のStringBuilder
     */
    override fun convert(clazz: IClass, sb: StringBuilder) {
        if(!isValidClass(clazz)){
            return
        }

        val firstStereotype = clazz.stereotypes?.firstOrNull() ?: ""

        sb.append(formatName(clazz))

        val stereotypes: List<String> = when {
            clazz is IUseCase -> filterStereotypes(clazz, listOf(STEREOTYPE_BUSINESS))
            firstStereotype == "actor" -> filterStereotypesForClass(clazz, listOf(STEREOTYPE_BUSINESS))
            else -> listOf()
        }

        sb.append(convertStereotype(stereotypes))

        sb.appendLine()
    }

    override fun isValidClass(clazz: IClass): Boolean {
        return clazz.stereotypes?.firstOrNull() !in excludeStereotypes
    }

    override fun formatName(clazz: IClass): String {
        //前処理、後処理しながらformatNameに渡す。
        if(clazz !is IUseCase && !clazz.hasStereotype("actor")) return formatName(clazz.name)
        var result = when{
            clazz is IUseCase -> "(${formatName(clazz.name)})"

            clazz.hasStereotype("actor") -> ":${formatName(clazz.name)}:"
            else -> formatName(clazz.name)
        }

        if(clazz.hasStereotype("business")) result += "/"

        return result
    }
}