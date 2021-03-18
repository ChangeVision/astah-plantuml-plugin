package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantStateDiagramConverter {
    fun convert(diagram: IStateMachineDiagram, sb: StringBuilder) {
        val rootVertexes =
            diagram.presentations
                .filterIsInstance<INodePresentation>().map { it.model }
                .filterIsInstance<IVertex>().filter { !(it.container is IVertex) }
        rootVertexes.forEach { vertexConvert(it, sb, "") }
        val transitions = diagram.presentations
            .filterIsInstance<ILinkPresentation>().map { it.model }
            .filterIsInstance<ITransition>()
        transitions.forEach { transitionConvert(it, sb) }
    }


    private fun vertexConvert(vertex: IVertex, sb: StringBuilder, indent: String) {
        when (vertex) {
            is IFinalState -> { /* Skip */
            }
            is IPseudostate -> { /* Skip */
            }
            is IState -> {
                when {
                    vertex.subvertexes.isEmpty() -> sb.appendLine("${indent}state ${vertex.name}")
                    else -> {
                        sb.appendLine("${indent}state ${vertex.name} {")
                        vertex.subvertexes.forEach { vertexConvert(it, sb, indent + "  ") }
                        sb.appendLine("${indent}}")
                    }
                }
            }
        }
    }

    private fun transitionConvert(transition: ITransition, sb: java.lang.StringBuilder) {
        sb.append(
            when (val source = transition.source) {
                is IPseudostate -> "[*]"
                is IFinalState -> "[*]"
                is IState -> source.name
                else -> "[*]"
            }
        )
        sb.append(" --> ")
        sb.append(
            when (val target = transition.target) {
                is IPseudostate -> "[*]"
                is IFinalState -> "[*]"
                is IState -> target.name
                else -> "[*]"
            }
        )
        val label =
            transition.event.let { if (it.isNotBlank()) it else "" } +
                    transition.guard.let { if (it.isNotBlank()) "[$it]" else "" } +
                    transition.action.let { if (it.isNotBlank()) "/$it" else "" }
        if (label.isNotBlank()) sb.append(": $label")
        sb.appendLine()
    }
}