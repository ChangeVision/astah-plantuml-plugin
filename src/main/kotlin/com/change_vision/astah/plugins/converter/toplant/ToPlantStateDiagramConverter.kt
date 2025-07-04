package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.IFinalState
import com.change_vision.jude.api.inf.model.IPseudostate
import com.change_vision.jude.api.inf.model.IState
import com.change_vision.jude.api.inf.model.IStateMachineDiagram
import com.change_vision.jude.api.inf.model.ITransition
import com.change_vision.jude.api.inf.model.IVertex
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantStateDiagramConverter {

    private val createdVertexes = ArrayList<IVertex>()
    private val createdTransitions = ArrayList<ITransition>()
    private val transitions = ArrayList<ITransition>()

    fun convert(diagram: IStateMachineDiagram, sb: StringBuilder) {
        createdVertexes.clear()
        createdTransitions.clear()
        transitions.clear()
        val rootVertexes =
            diagram.presentations
                .filterIsInstance<INodePresentation>().map { it.model }
                .filterIsInstance<IVertex>().filter { it.container !is IVertex }
        transitions.addAll(diagram.presentations
            .filterIsInstance<ILinkPresentation>().map { it.model }
            .filterIsInstance<ITransition>())
        rootVertexes.forEach { vertexConvert(it, sb, "") }
        transitions.forEach { transitionConvert(it, sb, "") }
    }


    private fun vertexConvert(vertex: IVertex, sb: StringBuilder, indent: String) {
        when (vertex) {
            is IFinalState -> { /* Skip */
                sb.appendLine("${indent}state \"${vertex.name}\" <<end>>")
                createdVertexes.add(vertex)
            }
            is IPseudostate -> { /* Skip */
                when {
                    vertex.isEntryPointPseudostate -> {
                        sb.appendLine("${indent}state \"${vertex.name}\" <<entryPoint>>")
                        createdVertexes.add(vertex)
                    }
                    vertex.isExitPointPseudostate -> {
                        sb.appendLine("${indent}state \"${vertex.name}\" <<exitPoint>>")
                        createdVertexes.add(vertex)
                    }
                    vertex.isChoicePseudostate -> {
                        sb.appendLine("${indent}state \"${vertex.name}\" <<choice>>")
                        createdVertexes.add(vertex)
                    }
                }
            }
            is IState -> {
                val subVertexes = vertex.subvertexes
                when {
                    subVertexes.isEmpty() -> sb.appendLine("${indent}state \"${vertex.name}\"")
                    else -> {
                        sb.appendLine("${indent}state ${vertex.name} {")
                        subVertexes.forEach {
                            vertexConvert(it, sb, "$indent  ")
                        }
                        sb.appendLine("")
                        transitions.forEach { transition ->
                            val source = transition.source
                            val target = transition.target
                            if (subVertexes.contains(source) && subVertexes.contains(target)) {
                                transitionConvert(transition, sb, "$indent  ")
                            }
                        }
                        sb.appendLine("${indent}}")
                    }
                }
            }
        }
    }

    private fun transitionConvert(transition: ITransition, sb: java.lang.StringBuilder, indent: String) {
        if (createdTransitions.contains(transition)) {
            return
        }
        sb.append(convertTransitionEnd(transition.source, indent))
        sb.append(" --> ")
        sb.append(convertTransitionEnd(transition.target, ""))
        createdTransitions.add(transition)
        val label =
            transition.event.let { it.ifBlank { "" } } +
                    transition.guard.let { if (it.isNotBlank()) "[$it]" else "" } +
                    transition.action.let { if (it.isNotBlank()) "/$it" else "" }
        if (label.isNotBlank()) sb.append(": $label")
        sb.appendLine()
    }

    private fun convertTransitionEnd(vertex : IVertex, indent : String) : String {
        return when (vertex) {
            is IPseudostate -> {
                if (vertex.isShallowHistoryPseudostate) {
                    "${indent}[H]"
                } else if (vertex.isDeepHistoryPseudostate) {
                    "${indent}[H*]"
                } else if (createdVertexes.contains(vertex)) {
                    // 作成済みの場合は、名前を指定する
                    "${indent}${vertex.name}"
                } else {
                    "${indent}[*]"
                }
            }
            is IFinalState -> {
                if (createdVertexes.contains(vertex)) {
                    // 作成済みの場合は、名前を指定する
                    "${indent}${vertex.name}"
                } else {
                    "${indent}[*]"
                }
            }
            is IState -> {
                "${indent}${vertex.name}"
            }
            else -> {
                if (createdVertexes.contains(vertex)) {
                    // 作成済みの場合は、名前を指定する
                    "${indent}${vertex.name}"
                } else {
                    "${indent}[*]"
                }
            }
        }
    }
}