package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.link.IAssociationConverter
import com.change_vision.jude.api.inf.model.IClass

/**
 * ユースケース図の関連を変換するクラス
 */
object AssociationConverter : IAssociationConverter {
    override fun formatName(clazz: IClass): String {
        return UseCaseConverter.formatName(clazz)
    }

    override fun getNonNavigableHat(): String {
        return ""
    }

    override fun isValidAssociation(end1: IClass, end2: IClass): Boolean {
        return end1.stereotypes?.firstOrNull() !in UseCaseConverter.excludeStereotypes &&
                end2.stereotypes?.firstOrNull() !in UseCaseConverter.excludeStereotypes
    }
}