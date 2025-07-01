package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ActivityConvertUtil {
    fun getIncomings(node: INodePresentation): List<ILinkPresentation> =
        node.links
            .filterIsInstance<ILinkPresentation>()
            .filter { it.type == "ControlFlow/ObjectFlow" }
            .filter { it.target == node }

    fun getOutgoings(node: INodePresentation): List<ILinkPresentation> =
        node.links
            .filterIsInstance<ILinkPresentation>()
            .filter { it.type == "ControlFlow/ObjectFlow" }
            .filter { it.source == node }

    fun isFirstNode(presentation: INodePresentation): Boolean {
        return getIncomings(presentation).isEmpty()
    }

    fun getNotes(node: INodePresentation) : List<INodePresentation>{
        val noteAnchors = node.links
            .filter { it.type == "NoteAnchor" }

        val results = mutableListOf<INodePresentation>()
        for (noteAnchor in noteAnchors){
            if(noteAnchor.source == node){ results.add(noteAnchor.target)
            }else results.add(noteAnchor.source)
        }
        return results
    }
}