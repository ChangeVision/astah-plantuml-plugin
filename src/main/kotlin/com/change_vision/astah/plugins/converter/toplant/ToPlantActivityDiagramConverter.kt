package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.IAction
import com.change_vision.jude.api.inf.model.IActivityDiagram
import com.change_vision.jude.api.inf.model.IActivityNode
import com.change_vision.jude.api.inf.model.IFlow
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantActivityDiagramConverter {
    fun convert(diagram: IActivityDiagram, sb: StringBuilder) {
        sb.append("(*) ")
        diagram.presentations
            .filterIsInstance<INodePresentation>().map { it.model }
            .filterIsInstance<IActivityNode>()
            .forEach { actionConvert(it, sb) }
        sb.appendLine("--> (*)")

    }

    private fun actionConvert(node: IActivityNode, sb: StringBuilder) {
        when (node) {
            is IAction -> sb.appendLine("--> \"${node.name}\"")
            else -> {
            }/* Not Support */
        }
    }

    private fun flowConvert(flow: IFlow, sb: StringBuilder) {
        sb.append(
            when (val source = flow.source) {
                is IActivityNode -> source.name
                else -> "[*]"
            }
        )
        sb.append(" --> ")
        sb.append(
            when (val target = flow.target) {
                is IActivityNode -> target.name
                else -> "[*]"
            }
        )

        sb.appendLine()
    }
}