package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.ILifeline
import com.change_vision.jude.api.inf.model.IMessage
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantSequenceDiagramConverter {
    fun convert(diagram: ISequenceDiagram, sb: StringBuilder) {
        // ライフラインはX軸、メッセージはY軸順にソートして出力する。
        val nodes = diagram.presentations.filterIsInstance<INodePresentation>().sortedBy { it.location.x }
        val links = diagram.presentations.filterIsInstance<ILinkPresentation>().sortedBy { it.allPoints.minOf { it.y } }
        nodes.forEach { node ->
            val model = node.model as? INamedElement
            if (model != null) {
                when {
                    model.stereotypes.contains("actor") -> sb.appendLine("actor " + model.name)
                    model.stereotypes.contains("entity") -> sb.appendLine("entity " + model.name)
                    model.stereotypes.contains("boundary") -> sb.appendLine("boundary " + model.name)
                    model.stereotypes.contains("control") -> sb.appendLine("control " + model.name)
                }
            }
        }
        links.forEach { link ->
            val model = link.model as IMessage
            val src = model.source.name.ifBlank { (model.source as ILifeline).base.name }
            val trg = model.target.name.ifBlank { (model.target as ILifeline).base.name }

            when {
                model.isAsynchronous -> sb.append("$src ->> $trg")
                model.isReturnMessage -> sb.append("$trg <-- $src")
                model.isSynchronous -> sb.append("$src -> $trg")
                model.isCreateMessage -> {
                    sb.appendLine("create $trg")
                    sb.append("$src -> $trg")
                }
            }
            if (model.name.isNotBlank()) {
                sb.appendLine(":" + model.name)
            } else {
                sb.appendLine()
            }
        }
    }
}