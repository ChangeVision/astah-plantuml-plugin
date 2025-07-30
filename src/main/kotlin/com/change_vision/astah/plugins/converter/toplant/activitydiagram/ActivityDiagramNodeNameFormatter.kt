package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.astah.plugins.converter.toplant.activitydiagram.ActivityDiagramNodeType.*
import com.change_vision.astah.plugins.converter.toplant.activitydiagram.syntax.LegacyActivityDiagramSyntax
import com.change_vision.astah.plugins.converter.toplant.node.NodeNameFormatter
import com.change_vision.jude.api.inf.presentation.INodePresentation

class ActivityDiagramNodeNameFormatter(
    private val idMap: Map<INodePresentation, String>
) : NodeNameFormatter {

    private val convertedNodes = mutableSetOf<INodePresentation>()

    override fun format(node: INodePresentation): String {
        return when {
            INITIAL_NODE(node) || FINAL_NODE(node) ->
                LegacyActivityDiagramSyntax.initialOrFinalNode()

            FORK_NODE(node) || JOIN_NODE(node) -> {
                val id = idMap[node]
                LegacyActivityDiagramSyntax.forkJoinNode(node.label, id)
            }

            FLOW_FINAL_NODE(node) ->{
                val id = idMap[node]
                LegacyActivityDiagramSyntax.node("フロー終了ノード", id)
            }

            else -> {
                val id = idMap[node]
                if (id != null) {
                    if (!convertedNodes.add(node)) {
                        return LegacyActivityDiagramSyntax.reuseId(id)
                    }
                    return LegacyActivityDiagramSyntax.node(label = node.label, alias = id)
                }
                return LegacyActivityDiagramSyntax.node(label = node.label)
            }
        }
    }
}
