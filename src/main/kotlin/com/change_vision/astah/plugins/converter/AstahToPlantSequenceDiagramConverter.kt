package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.model.ILifeline
import com.change_vision.jude.api.inf.model.IMessage
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object AstahToPlantSequenceDiagramConverter {
    fun convert(diagram: ISequenceDiagram, sb: StringBuilder) {
        diagram.presentations.forEach { presentation ->
            when (presentation) {
                is INodePresentation -> {
                    val model = presentation.model as? INamedElement
                    if (model != null) {
                        when {
                            model.stereotypes.contains("actor") -> sb.appendLine("actor " + model.name)
                            model.stereotypes.contains("entity") -> sb.appendLine("entity " + model.name)
                            model.stereotypes.contains("boundary") -> sb.appendLine("boundary " + model.name)
                            model.stereotypes.contains("control") -> sb.appendLine("control " + model.name)
                        }
                    }
                }
                is ILinkPresentation -> {
                    val model = presentation.model as IMessage
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
                else -> {
                }
            }
        }
    }
}