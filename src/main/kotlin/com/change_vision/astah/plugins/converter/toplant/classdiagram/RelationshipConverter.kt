package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.astah.plugins.converter.toplant.link.IRelationshipConverter
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement

/**
 * クラス図のリレーションシップ（継承・実現・依存）を変換するクラス
 */
object RelationshipConverter : IRelationshipConverter {
    override fun formatName(element: INamedElement): String {
        return ClassConverter.formatName(element)
    }

    override fun formatName(clazz: IClass): String {
        return ClassConverter.formatName(clazz)
    }
}