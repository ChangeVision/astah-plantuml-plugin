package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.jude.api.inf.presentation.INodePresentation

/**
 * INodePresentationのtypeを判断するクラス。
 * 必要な図で実装してください
* */
interface NodeType : (INodePresentation) -> Boolean {
    val labelText: String
    val idPrefix: String
}
