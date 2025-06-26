package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ActivityConvertUtil {
    fun getIncomings(node: INodePresentation): List<ILinkPresentation> =
        node.links
            .filterIsInstance<ILinkPresentation>()
            .filter { it.target == node }

    fun getOutgoings(node: INodePresentation): List<ILinkPresentation> =
        node.links
            .filterIsInstance<ILinkPresentation>()
            .filter { it.source == node }

    fun isFirstNode(presentation: INodePresentation): Boolean {
        return getIncomings(presentation).isEmpty()
    }
}