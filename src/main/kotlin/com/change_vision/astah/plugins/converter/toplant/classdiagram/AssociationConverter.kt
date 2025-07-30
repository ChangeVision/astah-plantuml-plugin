package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.astah.plugins.converter.toplant.link.IAssociationConverter
import com.change_vision.jude.api.inf.model.IClass

/**
 * クラス図の関連を変換するクラス
 */
object AssociationConverter : IAssociationConverter  {
    override fun formatName(clazz: IClass): String {
        return ClassConverter.formatName(clazz)
    }
}