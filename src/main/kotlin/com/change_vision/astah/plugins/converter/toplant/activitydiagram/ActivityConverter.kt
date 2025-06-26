package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.astah.plugins.converter.toplant.activitydiagram.syntax.LegacyActivityDiagramSyntax
import com.change_vision.astah.plugins.converter.toplant.node.NodeIdAssigner
import com.change_vision.astah.plugins.converter.toplant.node.NodeNameFormatter
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ActivityConverter {
    private val convertedLinks = mutableSetOf<ILinkPresentation>()
    private lateinit var nodeLabelFormatter: NodeNameFormatter

    fun clearConvertedLinks(){
        convertedLinks.clear()
    }

    fun initNodeNameFormatter(nodes : List<INodePresentation>){
        nodeLabelFormatter = ActivityDiagramNodeNameFormatter(createIdMap(nodes))
    }

    fun convert(startPresentation : INodePresentation, sb : StringBuilder){
        for (line in convertLines(startPresentation)) {
            sb.appendLine(line)
        }
    }

    private fun convertLines(presentation: INodePresentation): List<String> {
        val lines = mutableListOf<String>()
        val neededSourceLabel = ActivityConvertUtil.isFirstNode(presentation)
        val isDecisionNode = ActivityDiagramNodeType.DECISION_NODE(presentation)
        val outgoings = ActivityConvertUtil.getOutgoings(presentation)
        val firstOutgoing = outgoings.firstOrNull()
        for (outgoing in outgoings) {
            if (convertedLinks.contains(outgoing)) {
                continue
            }

            if (isDecisionNode) {
                if (firstOutgoing == outgoing) {
                    val incoming = ActivityConvertUtil.getIncomings(presentation).first()
                    val source = incoming.source
                    val sourceLabel = source
                        .takeIf { ActivityConvertUtil.isFirstNode(it) }
                        ?.let { nodeLabelFormatter.format(it) }
                    lines += LegacyActivityDiagramSyntax.ifBlock(sourceLabel, getLinkLabel(incoming))
                } else {
                    lines += LegacyActivityDiagramSyntax.elseBlock()
                }
            }

            val target = outgoing.target
            if (!ActivityDiagramNodeType.DECISION_NODE(target)) {
                val source =
                    if ((neededSourceLabel || firstOutgoing != outgoing) && !isDecisionNode)
                        nodeLabelFormatter.format(presentation)
                    else null
                val targetLabel = nodeLabelFormatter.format(target)
                val guard = getLinkLabel(outgoing).takeIf { it.isNotBlank() }
                lines += LegacyActivityDiagramSyntax.flow(source = source, target = targetLabel, guard = guard)
                convertedLinks.add(outgoing)
            }

            lines += convertLines(target)
        }

        if (isDecisionNode) {
            lines += LegacyActivityDiagramSyntax.endIfBlock()
        }

        return lines

    }

    private fun getLinkLabel(link: ILinkPresentation): String {
        return link.label.replace("[", "").replace("]", "")
    }

    private fun createIdMap(nodes: List<INodePresentation>): Map<INodePresentation, String> {
        val assigner = NodeIdAssigner(
            listOf(
                ActivityDiagramNodeType.ACTION,
                ActivityDiagramNodeType.OBJECT_NODE,
                ActivityDiagramNodeType.FORK_NODE,
                ActivityDiagramNodeType.JOIN_NODE,
                ActivityDiagramNodeType.DECISION_MERGE_NODE
            )
        )
        return assigner.assign(nodes)
    }

}