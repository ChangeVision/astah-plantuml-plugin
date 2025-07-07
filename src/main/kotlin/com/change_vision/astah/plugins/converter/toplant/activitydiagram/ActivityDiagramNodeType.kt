package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.astah.plugins.converter.toplant.node.NodeType
import com.change_vision.jude.api.inf.model.IAction
import com.change_vision.jude.api.inf.model.IControlNode
import com.change_vision.jude.api.inf.model.IObjectNode
import com.change_vision.jude.api.inf.presentation.INodePresentation

enum class ActivityDiagramNodeType(
    override val labelText: String,
    private val predicate: (INodePresentation) -> Boolean
) : NodeType {

    ACTION("Action", { it.model is IAction }),
    OBJECT_NODE("Object Node", { it.model is IObjectNode }),
    FORK_NODE("Fork Node", { (it.model as? IControlNode)?.isForkNode == true }),
    JOIN_NODE("Join Node", { (it.model as? IControlNode)?.isJoinNode == true }),
    INITIAL_NODE("Initial Node", { (it.model as? IControlNode)?.isInitialNode == true }),
    FINAL_NODE("Final Node", { (it.model as? IControlNode)?.isFinalNode == true }),
    FLOW_FINAL_NODE( "Flow Final Node",  { (it.model as? IControlNode)?.isFlowFinalNode == true }),
    DECISION_MERGE_NODE("Decision/Merge Node", { (it.model as? IControlNode)?.isDecisionMergeNode == true }),
    DECISION_NODE("Decision Node", {
        DECISION_MERGE_NODE(it)
                && ActivityConvertUtil.getIncomings(it).size == 1
                && ActivityConvertUtil.getOutgoings(it).size >= 2
    }),
    ;

    override fun invoke(node: INodePresentation): Boolean = predicate(node)

    override val idPrefix: String
        get() = when (this) {
            FINAL_NODE -> "X"
            else -> name.first().toString().lowercase()
        }
}
