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
}