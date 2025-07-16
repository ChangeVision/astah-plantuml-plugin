package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.jude.api.inf.model.IClass
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
        // 図のタイトルとして頭の名前を設定する
        convertTitle(diagram, sb)

        // ライフラインはX軸、メッセージはY軸順にソートして出力する。
        convertLifeLine(diagram, sb)

        // 複合フラグメントを出力する
        convertCombinedFragment(diagram, sb)
    }

    private fun convertTitle(diagram: ISequenceDiagram, sb: StringBuilder) {
        sb.appendLine("title " + ClassConverter.formatName(diagram.name))
        sb.appendLine()
    }

    private fun getCombinedFragmentPresentations(diagram: ISequenceDiagram) : List<INodePresentation> {
        return diagram.presentations.filterIsInstance<INodePresentation>()
                    .filter { it.model is ICombinedFragment }
                    .sortedBy { it.location.y }
    }

    private fun convertLifeLine(diagram: ISequenceDiagram, sb: StringBuilder) {
        val nodes = diagram.presentations.filterIsInstance<INodePresentation>().sortedBy { it.location.x }
        nodes.forEach { node ->
            val model = node.model as? INamedElement
            if (model is ILifeline) {
                val base = model.base

                // 名前を取得できなかったライフラインは作成しない
                val lifeLineName = getLifelineName(model) ?: return@forEach

                val lifeLineColor = node.getProperty("fill.color")

                if (base == null || base.stereotypes.isNullOrEmpty()) {
                    sb.appendLine("participant $lifeLineName $lifeLineColor")
                } else {
                    val stereotypes = getStereotypes(base)
                    val firstStereotype = base.stereotypes.first()
                    when {
                        firstStereotype == "actor" -> sb.appendLine("actor $lifeLineName $stereotypes$lifeLineColor")
                        firstStereotype == "entity" -> sb.appendLine("entity $lifeLineName $stereotypes$lifeLineColor")
                        firstStereotype == "boundary" -> sb.appendLine("boundary $lifeLineName $stereotypes$lifeLineColor")
                        firstStereotype == "control" -> sb.appendLine("control $lifeLineName $stereotypes$lifeLineColor")
                        else -> sb.appendLine("participant $lifeLineName $stereotypes$lifeLineColor")
                    }
                }
            }
        }
        sb.appendLine()
    }

    private fun getLifelineName(model: ILifeline): String? {
        val base = model.base
        if (!model.name.isNullOrEmpty()) {
            return ClassConverter.formatName(model.name)
        } else if (base != null && !base.name.isNullOrEmpty()) {
            return ClassConverter.formatName(base.name )
        }
        return null
    }

    private fun getStereotypes(base: IClass) : String {
        val stereotypeList = listOf("actor", "entity", "boundary", "control")
        val stereotypes = base.stereotypes
                            .filterIndexed { index, stereotype -> !(index == 0 && (stereotype in stereotypeList)) }
                            .joinToString(separator = "") { "<<${it}>>" }
        if (stereotypes.isEmpty()) {
            return ""
        }
        return "$stereotypes "
    }

    private fun convertCombinedFragment(diagram: ISequenceDiagram, sb: StringBuilder) {
        val combinedFragments = getCombinedFragmentPresentations(diagram)

        fragmentIndex = 0
        linkIndex = 0
        if (createdFragments.isNotEmpty()) {
            createdFragments.clear()
        }

        // y 座標基準でソートして取得する
        val links = diagram.presentations.filterIsInstance<ILinkPresentation>().sortedBy { it -> it.allPoints.minOf { it.y } }
        while ( linkIndex < links.size) {
            val link = links[linkIndex]
            val model = link.model as IMessage
            if (fragmentIndex >= combinedFragments.size) {
                convertMessage(model, link.getProperty("line.color"), sb)
                continue
            }
            val fragment = combinedFragments[fragmentIndex]
            if (link.allPoints.minOf { it.y } < fragment.location.y) {
                convertMessage(model, link.getProperty("line.color"), sb)
                continue
            }

            // 複合フラグメントの作成
            convertCombinedFragment(fragment, sb, links, combinedFragments)
        }
    }

    // astah -> PlantUML に複合フラグメントを変換する
    private fun convertCombinedFragment(fragment: INodePresentation, sb: StringBuilder, links:  List<ILinkPresentation>, combinedFragments: List<INodePresentation>) {
        // 子の複合フラグメントを取得
        val childrenFragments = getChildrenCombinedFragment(fragment, combinedFragments)

        val fragmentModel = fragment.model as ICombinedFragment
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
                val children = mutableListOf<IPresentation>()

                links.forEach { linkPresentation ->
                    operand.messages.forEach { operandMessage ->
                        if (linkPresentation.model == operandMessage) {
                            children.add(linkPresentation)
                        }
                    }
                }
                children.addAll(childrenFragments)
                children.sortWith(SequenceCompare())

                children.forEach { child ->
                    if (child is INodePresentation) {
                        convertCombinedFragment(child, sb, links, combinedFragments)
                        createdFragments.add(child)
                    } else if (child is ILinkPresentation) {
                        val childModel = child.model
                        if (childModel is IMessage) {

                            convertMessage(childModel, child.getProperty("line.color"), sb)
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

    private fun getChildrenCombinedFragment(parentFragment : INodePresentation, fragments : List<INodePresentation>) : MutableList<INodePresentation> {
        val result = mutableListOf<INodePresentation>()
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
    private fun convertMessage(model : IMessage, color : String?, sb: StringBuilder) {
        val src = when (val source = model.source) {
                    is ILifeline -> getLifelineName(source)
                    else -> ""
        }
        val trg = when (val target = model.target) {
                    is ILifeline -> getLifelineName(target)
                    else -> ""
        }
        if (src == null || trg == null) {
            // 接続先を取得できないメッセージは作成せずに処理済みとする
            linkIndex++
            return
        }

        val convertColor = if (color.isNullOrEmpty()) ""
                           else "[$color]"

        when {
            model.isAsynchronous -> sb.append("$src -$convertColor>> $trg")
            model.isReturnMessage -> sb.append("$src <-$convertColor- $trg")
            model.isCreateMessage -> {
                sb.appendLine("create $trg")
                sb.append("$src -$convertColor> $trg")
            }
            model.isDestroyMessage -> sb.append("$src -$convertColor> $trg !!")
            model.isSynchronous -> sb.append("$src -$convertColor> $trg")
        }
        if (model.name.isNotBlank()) {
            sb.appendLine(":" + model.name)
        } else {
            sb.appendLine()
        }
        linkIndex++
    }

    private class SequenceCompare : Comparator<IPresentation> {

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
}