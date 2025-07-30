package com.change_vision.astah.plugins.converter.toplant.node

import com.change_vision.jude.api.inf.presentation.INodePresentation

/** 同名図要素があったとき、それをエイリアスで表現することでplantUMLに変換できるようにするためのクラス
 * 必要な図で実装してください
* */
interface NodeNameFormatter {
    fun format(node: INodePresentation): String
}