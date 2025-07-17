package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.node.IClassConverter
import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IUseCase

object UseCaseConverter : IClassConverter {
    private const val STEREOTYPE_BUSINESS = "business"

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz ユースケースかアクター
     * @param sb 出力用のStringBuilder
     */
    override fun convert(clazz: IClass, sb: StringBuilder) {
        val firstStereotype = clazz.stereotypes?.firstOrNull() ?: ""
        if (firstStereotype in listOf("entity", "boundary", "control")) {
            return
        }

        sb.append(ClassConverter.formatName(clazz))

        val stereotypes: List<String> = when {
            clazz is IUseCase -> filterStereotypes(clazz, listOf(STEREOTYPE_BUSINESS))
            firstStereotype == "actor" -> filterStereotypesForClass(clazz, listOf(STEREOTYPE_BUSINESS))
            else -> listOf()
        }

        sb.append(convertStereotype(stereotypes))

        sb.appendLine()
    }
}