package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.astah.plugins.converter.toplant.activitydiagram.ActivityConvertUtil
import com.change_vision.astah.plugins.converter.toplant.activitydiagram.ActivityConverter
import com.change_vision.astah.plugins.converter.toplant.activitydiagram.ActivityDiagramNodeType.INITIAL_NODE
import com.change_vision.jude.api.inf.model.IActivityDiagram
import com.change_vision.jude.api.inf.model.IActivityNode
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantActivityDiagramConverter {

    fun convert(diagram: IActivityDiagram, sb: StringBuilder) {
        val nodes = diagram.presentations
            .filterIsInstance<INodePresentation>()
            .filter { it.model is IActivityNode }

        ActivityConverter.clearConvertedPresentations()
        val startPresentations = nodes
            .filter { ActivityConvertUtil.isFirstNode(it) }
            //開始ノードがリストの最初に来るようにソート
            .sortedBy { !INITIAL_NODE(it)}

        ActivityConverter.initNodeNameFormatter(nodes)
        for (startPresentation in startPresentations) {
            ActivityConverter.convert(startPresentation, sb)
        }
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