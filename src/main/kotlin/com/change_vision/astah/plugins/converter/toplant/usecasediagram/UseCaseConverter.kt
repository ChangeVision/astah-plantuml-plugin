package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IClass

object UseCaseConverter {
    /**
     * クラスをPlantUML形式に変換する
     * @param clazz ユースケースかアクター
     * @param sb 出力用のStringBuilder
     */
    fun convert(clazz: IClass, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(clazz))
        val stereotypes = clazz.stereotypes.filterNot {
            it.equals("interface", ignoreCase = true) ||
                    it.equals("enumeration", ignoreCase = true) ||
                    it.equals("actor", ignoreCase = true) ||
                    it.equals("business", ignoreCase = true)
        }

        if (stereotypes.isNotEmpty()) {
            sb.append(" <<")
            sb.append(stereotypes.joinToString(","))
            sb.append(">>")
        }
        sb.appendLine()
    }
}