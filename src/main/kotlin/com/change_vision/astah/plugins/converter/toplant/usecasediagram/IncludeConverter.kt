package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IInclude

object IncludeConverter {
    /**
     * 包含関係をPlantUML形式に変換する
     * @param model 包含関係
     * @param sb 出力用のStringBuilder
     */
    fun convert(model: IInclude, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(model.includingCase))
        sb.append(" .> ")
        sb.append(ClassConverter.formatName(model.addition))
        sb.append(" : include")
        sb.appendLine()
    }
}