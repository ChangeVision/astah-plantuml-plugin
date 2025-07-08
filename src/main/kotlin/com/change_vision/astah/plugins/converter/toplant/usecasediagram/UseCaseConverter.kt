package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.node.IClassConverter
import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IUseCase

object UseCaseConverter : IClassConverter {
    private val STEREOTYPE_BUSINESS = "business"

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz ユースケースかアクター
     * @param sb 出力用のStringBuilder
     */
    override fun convert(clazz: IClass, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(clazz))

        var stereotypes : List<String> = listOf()

        if(clazz is IUseCase){
            stereotypes =  filterStereotypes(clazz,listOf(STEREOTYPE_BUSINESS))
        }else if(clazz.hasStereotype("actor")){
            stereotypes =  filterStereotypesForClass(clazz,listOf(STEREOTYPE_BUSINESS))
        }

        sb.append(convertStereotype(stereotypes))

        sb.appendLine()
    }
}