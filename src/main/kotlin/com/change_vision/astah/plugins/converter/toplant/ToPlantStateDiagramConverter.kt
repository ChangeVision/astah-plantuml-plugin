package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.*
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
//                    vertex.isShallowHistoryPseudostate -> {
//                        sb.appendLine("${indent}[H]")
//                        createdVertexes.add(vertex)
//                    }
//                    vertex.isDeepHistoryPseudostate -> {
//                        sb.appendLine("${indent}[H*]")
//                        createdVertexes.add(vertex)
//                    }
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
        when (val source = transition.source) {
            is IPseudostate -> {
                if (source.isShallowHistoryPseudostate) {
                    sb.append("${indent}[H]")
                } else if (source.isDeepHistoryPseudostate) {
                    sb.append("${indent}[H*]")
                } else if (createdVertexes.contains(source)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${source.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
            is IFinalState -> {
                if (createdVertexes.contains(source)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${source.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
            is IState -> {
                sb.append("${indent}${source.name}")
            }
            else -> {
                if (createdVertexes.contains(source)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${source.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
        }
        sb.append(" --> ")
        when (val target = transition.target) {
            is IPseudostate -> {
                if (target.isShallowHistoryPseudostate) {
                    sb.append("${indent}[H]")
                } else if (target.isDeepHistoryPseudostate) {
                    sb.append("${indent}[H*]")
                } else if (createdVertexes.contains(target)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${target.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
            is IFinalState -> {
                if (createdVertexes.contains(target)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${target.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
            is IState -> {
                sb.append("${indent}${target.name}")
            }
            else -> {
                if (createdVertexes.contains(target)) {
                    // 作成済みの場合は、名前を指定する
                    sb.append("${indent}${target.name}")
                } else {
                    sb.append("${indent}[*]")
                }
            }
        }
        createdTransitions.add(transition)
        val label =
            transition.event.let { it.ifBlank { "" } } +
                    transition.guard.let { if (it.isNotBlank()) "[$it]" else "" } +
                    transition.action.let { if (it.isNotBlank()) "/$it" else "" }
        if (label.isNotBlank()) sb.append(": $label")
        sb.appendLine()
    }
}