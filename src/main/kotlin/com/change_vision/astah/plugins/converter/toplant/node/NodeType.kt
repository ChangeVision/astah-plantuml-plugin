package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.jude.api.inf.presentation.INodePresentation

/**
 * INodePresentationのtypeを判断するクラス。
 * 必要な図で実装してください
* */
interface NodeType : (INodePresentation) -> Boolean {
    val labelText: String
    val idPrefix: String

    companion object {
        /**
         * “Note” 要素かどうかを判定する実装。
         * これ自体が `(INodePresentation) -> Boolean` として呼び出せる。
         */
        val NOTE: NodeType = object : NodeType {
            override val labelText = "note"
            override val idPrefix = "n"          // 必要なら上書き
            override fun invoke(node: INodePresentation): Boolean =
                node.type == "Note" && !node.label.isNullOrBlank()
        }
    }
}
