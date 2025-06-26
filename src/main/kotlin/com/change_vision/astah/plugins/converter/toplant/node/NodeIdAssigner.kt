package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.astah.plugins.converter.toplant.activitydiagram.ActivityConvertUtil
import com.change_vision.jude.api.inf.presentation.INodePresentation

/**
 * モデル内の同種、同名INodePresentationを探し、一意なidを設定する
 * */
class NodeIdAssigner(
    private val nodeTypes: List<NodeType>
) {

    fun assign(nodes: List<INodePresentation>): Map<INodePresentation, String> {
        val idMap = mutableMapOf<INodePresentation, String>()
        for (type in nodeTypes) {
            idMap.putAll(assignIds(nodes, type))
        }
        return idMap
    }

    private fun assignIds(
        nodes: List<INodePresentation>,
        type: NodeType
    ): Map<INodePresentation, String> {
        val idMap = mutableMapOf<INodePresentation, String>()
        var id = 1
        for ((_, group) in nodes.filter(type).groupBy { it.label }) {
            for (node in group) {
                val shouldAssignId = group.size >= 2 ||
                        ActivityConvertUtil.getOutgoings(node).size > 1
                if (shouldAssignId) {
                    idMap.putIfAbsent(node, "${type.idPrefix}${id++}")
                }
            }
        }
        return idMap
    }
}
