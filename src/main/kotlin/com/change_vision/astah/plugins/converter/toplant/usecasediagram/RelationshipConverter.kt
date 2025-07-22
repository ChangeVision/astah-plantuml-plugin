package com.change_vision.astah.plugins.converter.toplant.usecasediagram

import com.change_vision.astah.plugins.converter.toplant.link.IRelationshipConverter
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement

object RelationshipConverter : IRelationshipConverter {
    override fun formatName(element: INamedElement): String {
        return UseCaseConverter.formatName(element)
    }

    override fun formatName(clazz: IClass): String {
        return UseCaseConverter.formatName(clazz)
    }
}