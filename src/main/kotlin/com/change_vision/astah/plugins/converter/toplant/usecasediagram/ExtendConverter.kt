package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IExtend

object ExtendConverter {
    /**
     * 拡張関係をPlantUML形式に変換する
     * @param model 拡張関係
     * @param sb 出力用のStringBuilder
     */
    fun convert(model: IExtend, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(model.extension))
        sb.append(" .> ")
        sb.append(ClassConverter.formatName(model.extendedCase))
        sb.append(" : extends")
        sb.appendLine()
    }
}