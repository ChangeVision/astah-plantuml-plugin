package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.ICombinedFragment
import com.change_vision.jude.api.inf.model.ILifeline
import com.change_vision.jude.api.inf.model.IMessage
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation
import com.change_vision.jude.api.inf.presentation.IPresentation

object ToPlantSequenceDiagramConverter {

    private var fragmentIndex = 0
    private var linkIndex = 0
    private val createdFragments = ArrayList<INodePresentation>()

    fun convert(diagram: ISequenceDiagram, sb: StringBuilder) {
        // ライフラインはX軸、メッセージはY軸順にソートして出力する。
        val presentations = diagram.presentations
        var nodes = diagram.presentations.filterIsInstance<INodePresentation>().sortedBy { it.location.x }
        val links = diagram.presentations.filterIsInstance<ILinkPresentation>().sortedBy { it.allPoints.minOf { it.y } }
        nodes.forEach { node ->
            val model = node.model as? INamedElement
            if (model != null) {
                when {
                    model.stereotypes.contains("actor") -> sb.appendLine("actor " + model.name)
                    model.stereotypes.contains("entity") -> sb.appendLine("entity " + model.name)
                    model.stereotypes.contains("boundary") -> sb.appendLine("boundary " + model.name)
                    model.stereotypes.contains("control") -> sb.appendLine("control " + model.name)
                    model is ILifeline -> sb.appendLine("participant " + model.name)
                }
            }
        }
        // y 座標基準でソートし直す
        nodes = nodes.sortedBy { it.location.y }
        // 複合フラグメントとオペランドを抽出
        val combinedFragments : ArrayList<INodePresentation> = ArrayList()
        nodes.forEach { node ->
            if (node.model is ICombinedFragment) {
                combinedFragments.add(node)
            }
        }

        fragmentIndex = 0
        linkIndex = 0
        if (createdFragments.isNotEmpty()) {
            createdFragments.clear()
        }

        while ( linkIndex < links.size) {
            val link = links[linkIndex]
            var model = link.model as IMessage
            if (fragmentIndex >= combinedFragments.size) {
                convertMessage(model, sb)
                continue
            }
            val fragment = combinedFragments[fragmentIndex]
            if (link.allPoints.minOf { it.y } < fragment.location.y) {
                convertMessage(model, sb)
                continue
            }

            // 複合フラグメントの作成
            convertCombinedFragment(fragment, sb, links, combinedFragments)
        }
    }

    // astah -> PlantUML に複合フラグメントを変換する
    private fun convertCombinedFragment(fragment : INodePresentation, sb : StringBuilder, links :  List<ILinkPresentation>, combinedFragments: ArrayList<INodePresentation>) {
        // 子の複合フラグメントを取得
        val childrenFragments = getChildrenCombinedFragment(fragment, combinedFragments)

        val fragmentModel: ICombinedFragment = fragment.model as ICombinedFragment
        fragmentModel.interactionOperands.forEachIndexed { operandIndex, operand ->
            if (operandIndex == 0) {
                when {
                    fragmentModel.isAlt -> sb.appendLine("alt " + operand.guard)
                    fragmentModel.isAssert -> sb.appendLine("group assert " + operand.guard)
                    fragmentModel.isBreak -> sb.appendLine("break " + operand.guard)
                    fragmentModel.isConsider -> sb.appendLine("group consider " + operand.guard)
                    fragmentModel.isCritical -> sb.appendLine("critical " + operand.guard)
                    fragmentModel.isIgnore -> sb.appendLine("group ignore " + operand.guard)
                    fragmentModel.isLoop -> sb.appendLine("loop " + operand.guard)
                    fragmentModel.isNeg -> sb.appendLine("group neg " + operand.guard)
                    fragmentModel.isOpt -> sb.appendLine("opt " + operand.guard)
                    fragmentModel.isPar -> sb.appendLine("par " + operand.guard)
                    fragmentModel.isSeq -> sb.appendLine("group seq " + operand.guard)
                    fragmentModel.isStrict -> sb.appendLine("group strict " + operand.guard)
                }
            } else if (operandIndex > 0) {
                sb.appendLine("else " + operand.guard)
            }
            if (operand.messages == null || operand.messages.isEmpty()) {
                childrenFragments.forEach { childFragment ->
                    if (!createdFragments.contains(childFragment)) {
                        convertCombinedFragment(childFragment, sb, links, combinedFragments)
                        createdFragments.add(childFragment)
                    }
                }
            } else {
                // 複合フラグメント内のメッセージと複合フラグメントを上から順番に並べる
                val children = ArrayList<IPresentation>()

                links.forEach { linkPresentation ->
                    operand.messages.forEach { operandMessage ->
                        if (linkPresentation.model == operandMessage) {
                            children.add(linkPresentation)
                        }
                    }
                }
                children.addAll(childrenFragments)
                children.sortWith(sequenceCompare())

                children.forEach { child ->
                    if (child is INodePresentation) {
                        convertCombinedFragment(child, sb, links, combinedFragments)
                        createdFragments.add(child)
                    } else if (child is ILinkPresentation) {
                        val childModel = child.model
                        if (childModel is IMessage) {
                            convertMessage(childModel, sb)
                        }
                    }
                }
            }
            if (createdFragments.isNotEmpty()) {
                childrenFragments.removeAll(createdFragments.toSet())
            }
        }
        sb.appendLine("end")
        fragmentIndex++

    }

    private class sequenceCompare : Comparator<IPresentation> {

        override fun compare(presentation1: IPresentation?, presentation2: IPresentation?): Int {
            var y1 = 0.0
            var y2 = 0.0
            if (presentation1 is INodePresentation) {
                y1 = presentation1.location.y
            } else if (presentation1 is ILinkPresentation) {
                y1 = presentation1.allPoints.minOf { it.y }
            }
            if (presentation2 is INodePresentation) {
                y2 = presentation2.location.y
            } else if (presentation2 is ILinkPresentation) {
                y2 = presentation2.allPoints.minOf { it.y }
            }
            return if (y1 < y2) {
                -1
            } else if (y2 < y1) {
                1
            } else {
                0
            }
        }

    }
    private fun getChildrenCombinedFragment(parentFragment : INodePresentation, fragments : List<INodePresentation>) : ArrayList<INodePresentation> {
        val result = ArrayList<INodePresentation>()
        var childIndex = fragmentIndex + 1
        while (childIndex < fragments.size) {
            val child = fragments[childIndex]
            if (parentFragment != child && parentFragment.rectangle.contains(child.rectangle)) {
                result.add(child)
            }
            childIndex++
        }
        return result
    }

    // astah -> PlantUML にメッセージを変換する
    private fun convertMessage(model : IMessage, sb: StringBuilder) : StringBuilder {
        val src = model.source.name.ifBlank { (model.source as ILifeline).base.name }
        val trg = model.target.name.ifBlank { (model.target as ILifeline).base.name }

        when {
            model.isAsynchronous -> sb.append("$src ->> $trg")
            model.isReturnMessage -> sb.append("$trg <-- $src")
            model.isSynchronous -> sb.append("$src -> $trg")
            model.isCreateMessage -> {
                sb.appendLine("create $trg")
                sb.append("$src -> $trg")
            }
        }
        if (model.name.isNotBlank()) {
            sb.appendLine(":" + model.name)
        } else {
            sb.appendLine()
        }
        linkIndex++
        return sb
    }
}