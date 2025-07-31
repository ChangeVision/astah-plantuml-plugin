package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.ICombinedFragment
//import com.change_vision.jude.api.inf.model.ICombinedFragment
import com.change_vision.jude.api.inf.model.IDiagram
import com.change_vision.jude.api.inf.model.ILifeline
import com.change_vision.jude.api.inf.model.IMessage
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation
//import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil
import net.sourceforge.plantuml.sequencediagram.AbstractMessage
import net.sourceforge.plantuml.sequencediagram.Event
import net.sourceforge.plantuml.sequencediagram.GroupingStart
import net.sourceforge.plantuml.sequencediagram.GroupingType.ELSE
import net.sourceforge.plantuml.sequencediagram.GroupingType.END
import net.sourceforge.plantuml.sequencediagram.GroupingLeaf
import net.sourceforge.plantuml.sequencediagram.Message
import net.sourceforge.plantuml.sequencediagram.MessageExo
import net.sourceforge.plantuml.sequencediagram.MessageExoType.FROM_RIGHT
import net.sourceforge.plantuml.sequencediagram.MessageExoType.FROM_LEFT
import net.sourceforge.plantuml.sequencediagram.MessageExoType.TO_RIGHT
import net.sourceforge.plantuml.sequencediagram.MessageExoType.TO_LEFT
import net.sourceforge.plantuml.sequencediagram.Participant
import net.sourceforge.plantuml.sequencediagram.ParticipantType
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram
import net.sourceforge.plantuml.skin.ArrowDecoration
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap


object ToAstahSequenceDiagramConverter {
    private const val X_SPAN = 20.0
    private const val Y_SPAN = 80.0
    private const val FOUND_POINT_SPAN = 100.0
    private const val LOST_POINT_SPAN = 100.0
    private const val INIT_Y = 50.0
    private const val LIFELINE_LENGTH_PROPERTY = 40.0
    private const val GROUP_OFFSET = 10.0
    private const val FRAME_TYPE = "Frame"
    private const val DEFAULT_ACTOR_NAME = "Actor"
    private const val DEFAULT_BOUNDARY_NAME = "Boundary"
    private const val DEFAULT_ENTITY_NAME = "Entity"
    private const val DEFAULT_CONTROL_NAME = "Control"

    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val modelEditor = projectAccessor.modelEditorFactory.basicModelEditor
    private val diagramEditor = projectAccessor.diagramEditorFactory.sequenceDiagramEditor
    private var groupingOffset = 0.0

    // astah で利用できる複合フラグメントのリスト
    private val combinedFragmentTypes: List<String> = listOf("alt", "opt", "par", "loop",
        "critical", "neg", "assert", "strict", "ignore", "consider", "seq", "ref", "break")

    fun convert(diagram: SequenceDiagram, index: Int, isMultiDiagrams: Boolean) {
        // create diagram
        val sequenceDiagram = createOrGetDiagram(index, DiagramKind.SequenceDiagram, isMultiDiagrams)

        // convert lifeline
        TransactionManager.beginTransaction()
        try {
            val participantMap = createLifeLinePresentations(diagram)
            val events = diagram.events()

            // convert combined fragments
            convertCombinedFragments(sequenceDiagram, events, participantMap)

            // convert messages
            convertMessage(sequenceDiagram, events, participantMap)
        } catch (e: Exception) {
            e.printStackTrace()
            TransactionManager.abortTransaction()
            return
        }
        TransactionManager.endTransaction()

        if (sequenceDiagram != null) {
            api.viewManager.diagramViewManager.open(sequenceDiagram)
        }
    }

    private fun convertCombinedFragments(sequenceDiagram: IDiagram?, events: List<Event>, participantMap: Map<Participant, INodePresentation>) {
        val groupingDeque = ArrayDeque<GroupingStart>()
        val elseDeque = ArrayDeque<GroupingLeaf>()
        val operandMessageMap = HashMap<Int, Int>()
        var operandIndex = 0
        groupingOffset = 0.0
        events.forEachIndexed { eventIndex, event ->
            when (event) {
                is GroupingStart -> {
                    groupingDeque.addLast(event)
                    groupingOffset += GROUP_OFFSET
                    operandMessageMap[operandIndex] = eventIndex
                    operandIndex++
                }
                is GroupingLeaf -> {
                    when (event.type) {
                        END -> {
                            if (groupingDeque.isEmpty()) {
                                return@forEachIndexed
                            }
                            val groupingStart = groupingDeque.removeLast()

                            val comment = groupingStart.comment ?: ""
                            val title = groupingStart.title

                            if (title !in combinedFragmentTypes) {
                                return@forEachIndexed
                            }

                            // TODO PlantUML で複合フラグメントの名前をつけられる記法への対応は今後実施する
                            val combinedFragmentName = ""

                            val rect =
                                getCombinedFragmentRectangle(sequenceDiagram, eventIndex, groupingStart, events, participantMap)
                            val point2D = Point2D.Double(rect.x, rect.y)

                            // TODO オペランドの高さを変更すると複合フラグメントが不正モデルとなるため、一旦オペランドの対応はしない
                            val fragmentPresentation =
                                diagramEditor.createCombinedFragment(combinedFragmentName, title, point2D, rect.width, rect.height)
                            val fragmentModel = fragmentPresentation.model as ICombinedFragment

                            val operands = fragmentModel.interactionOperands
                            if (!operands.isNullOrEmpty()) {
                                // TODO 複数オペランド対応を見送っているため、最初のオペランドのガードのみ設定する
                                operands[0].guard = comment
                            }
//                            if (elseDeque.isEmpty()) {
//                                return@forEachIndexed
//                            }
//                            val model = fragmentPresentation.model
//                            if (model is ICombinedFragment) {
//                                // 2個目以降のオペランドの高さを設定していく
//                                convertOperand(model, elseDeque, fragmentPresentation, groupingStart, events)
//                            }
                        }
                        ELSE -> {
                            elseDeque.add(event)
                            operandMessageMap[operandIndex] = eventIndex
                            operandIndex++
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // TODO オペランドの高さを変更すると複合フラグメントが不正モデルとなるため、一旦オペランドの対応はしない
//    private fun convertOperand(model: ICombinedFragment, elseDeque: ArrayDeque<GroupingLeaf>, fragmentPresentation: INodePresentation, groupingStart: GroupingStart, events: List<Event>) {
//        elseDeque.forEach { groupingElse ->
//            if (groupingElse.groupingStart != groupingStart) {
//                return@forEach
//            }
//            val elseTitle = groupingElse.title
//            var elseComment = groupingElse.comment
//            if (elseComment == null) {
//                elseComment = ""
//            }
//            model.addInteractionOperand(elseTitle, elseComment)
//        }
//        var operandPropertyIndex = 1
//        val processedElseDeque = ArrayDeque<GroupingLeaf>()
//        elseDeque.forEachIndexed { elseIndex, groupingElse ->
//            if (groupingElse.groupingStart != groupingStart) {
//                return@forEachIndexed
//            }
//            if (elseIndex == 0) {
//                // オペランドが追加された時点で GroupingStart で始まる1個目のオペランドの高さを設定する
//                adjustOperandHeight(events, groupingStart, fragmentPresentation, events.indexOf(groupingStart) + 1, operandPropertyIndex)
//                operandPropertyIndex++
//            }
//            adjustOperandHeight(events, groupingStart, fragmentPresentation, events.indexOf(groupingElse) + 1, operandPropertyIndex)
//            operandPropertyIndex++
//            processedElseDeque.add(groupingElse)
//        }
//        // elseDeque から処理が終わったものを削除
//        processedElseDeque.forEach{ processedItem ->
//            elseDeque.remove(processedItem)
//        }
//    }
//
//    private fun adjustOperandHeight(events: List<Event>, groupingStart: GroupingStart, fragmentPresentation: INodePresentation, messagePosition: Int, operandPropertyIndex: Int) {
//        val properties = fragmentPresentation.properties
//        var operandMessageCount = 0
//        var messageIndex = messagePosition
//
//        // オペランド内のメッセージをカウントして、メッセージ数に応じたオペランドの高さを計算して設定する
//        while (!(events[messageIndex] is GroupingLeaf
//                    && (events[messageIndex] as GroupingLeaf).groupingStart == groupingStart)
//        ) {
//            if (events[messageIndex] is Message || events[messageIndex] is MessageExo) {
//                operandMessageCount++
//            }
//            messageIndex++
//        }
//        val operandKey = PresentationPropertyUtil.createOperandLengthKey(operandPropertyIndex)
//        if (properties.keys.contains(operandKey)) {
//            val value = (Y_SPAN * operandMessageCount).toString()
//            fragmentPresentation.setProperty(operandKey, value)
//        }
//    }

    private fun getCombinedFragmentPositionX(lifelines: MutableList<INodePresentation>): Double {
        var x = 0.0
        // 複合フラグメント内の最左端のライフラインを探す
        // 複合フラグメントの x 位置は最左端のライフラインと同じ位置に設定する
        lifelines.forEach { lifeline ->
            if (lifeline.location.x == 0.0) {
                // location.x が 0.0 のライフラインは最左端であるため、1.0 を返す
                return 1.0
            }
            if (x == 0.0) {
                x = lifeline.location.x
            } else if (lifeline.location.x < x) {
                x = lifeline.location.x
            }
        }
        return x
    }

    private fun getCombinedFragmentPositionY(events: List<Event>, firstMessage: AbstractMessage?): Double {
        var firstMessageIndex = 0
        // 複合フラグメント内の最初のメッセージまでのメッセージの数をカウントする
        run loop@{
            events.forEach { e ->
                if (e == firstMessage) {
                    // 複合フラグメント内の最初のメッセージを見つけた場合、ループを抜ける
                    return@loop
                }
                if (e is Message || e is MessageExo) {
                    firstMessageIndex++
                }
            }
        }
        return INIT_Y + Y_SPAN * (firstMessageIndex + 1.0) - 20
    }

    private fun getCombinedFragmentWidth(sequenceDiagram: IDiagram?,
                                         lifelines: MutableList<INodePresentation>,
                                         isRight: Boolean,
                                         isLeft: Boolean,
                                         presentations: Collection<INodePresentation>,
                                         groupingOffset: Double): Double {
        lifelines.sortWith(LifelineCompare())
        val mostLeftLifeline = lifelines.first()
        val mostRightLifeline = lifelines.last()

        var width = 0.0
        if (isRight && isLeft) {
            sequenceDiagram?.presentations?.forEach { presentation ->
                if (!presentations.contains(presentation)) {
                    width = (presentation as INodePresentation).width - 2.0
                }
            }
        } else if (isRight) {
            sequenceDiagram?.presentations?.forEach { presentation ->
                if (!presentations.contains(presentation)) {
                    width = (presentation as INodePresentation).width - mostLeftLifeline.location.x
                }
            }
        } else if (isLeft) {
            width = mostRightLifeline.location.x + mostRightLifeline.width
        } else {
            width = mostRightLifeline.location.x + mostRightLifeline.width - mostLeftLifeline.location.x
        }
        width -= groupingOffset * 2
        return width
    }

    private fun getCombinedFragmentRectangle(sequenceDiagram: IDiagram?, index: Int, groupingStart: GroupingStart, events: List<Event>, participantMap: Map<Participant, INodePresentation>): Rectangle2D {
        var groupIndex = events.indexOf(groupingStart) + 1

        var messageCount = 0
        val lifelines = mutableListOf<INodePresentation>()
        var firstMessage: AbstractMessage? = null

        // フレーム左端を起点・終点にするメッセージを含む場合に true
        var isLeft = false
        // フレーム右端を起点・終点にするメッセージを含む場合に true
        var isRight = false

        // 複合フラグメントの位置と大きさを計算
        while (groupIndex < index) {
            val innerFragmentEvent = events[groupIndex]
            if (innerFragmentEvent is Message && participantMap.isNotEmpty()) {
                val lifeline1: INodePresentation? = participantMap[innerFragmentEvent.participant1]
                if (lifeline1 != null && !lifelines.contains(lifeline1)) {
                    lifelines.add(lifeline1)
                }
                val lifeline2: INodePresentation? = participantMap[innerFragmentEvent.participant2]
                if (lifeline2 != null && !lifelines.contains(lifeline2)) {
                    lifelines.add(lifeline2)
                }
                if (firstMessage == null) {
                    firstMessage = innerFragmentEvent
                }
                messageCount++
            } else if (innerFragmentEvent is MessageExo) {
                val lifeline = participantMap[innerFragmentEvent.participant]
                if (lifeline != null && !lifelines.contains(lifeline)) {
                    lifelines.add(lifeline)
                }
                if (innerFragmentEvent.type == FROM_LEFT || innerFragmentEvent.type == TO_LEFT) {
                    isLeft = true
                } else if (innerFragmentEvent.type == FROM_RIGHT || innerFragmentEvent.type == TO_RIGHT) {
                    isRight = true
                }
                if (firstMessage == null) {
                    firstMessage = innerFragmentEvent
                }
                messageCount++
            }
            groupIndex++
        }

        val rect = Rectangle2D.Double()
        rect.x = if (isLeft) 1.0
                 else getCombinedFragmentPositionX(lifelines)
        rect.y = getCombinedFragmentPositionY(events, firstMessage)

        if (groupingOffset > 0.0) {
            groupingOffset -= GROUP_OFFSET
            if (0.0 > groupingOffset) {
                groupingOffset = 0.0
            }
        }
        val presentations = participantMap.values
        rect.x += groupingOffset
        rect.width = getCombinedFragmentWidth(sequenceDiagram, lifelines, isRight, isLeft, presentations, groupingOffset)
        rect.height = Y_SPAN * (messageCount - 1) + 40
        return rect
    }

    private fun createLifeLinePresentations(diagram: SequenceDiagram) : Map<Participant, INodePresentation> {
        var prevX = 0.0
        val totalMessageCount = diagram.events().count { event -> event is Message || event is MessageExo }
        val lifelineLength = INIT_Y + Y_SPAN * totalMessageCount + LIFELINE_LENGTH_PROPERTY

        val participantMap = diagram.participants().mapNotNull { participant ->
            val isAlias = !participant.code.equals(participant.getDisplay(false).toTooltipText())

            // PlantUML 上で定義されている名前を一旦ベースクラス名候補として取得する
            var baseClassName = if (isAlias) participant.getDisplay(false).toTooltipText()
                                else participant.code

            // 取得したベースクラス名候補を ":" でパースし、":" 以前をライフライン名、 ":" 以降をベースクラス名とする
            val coronIndex = baseClassName.lastIndexOf(":")
            var lifelineName = if (coronIndex == baseClassName.length - 1) baseClassName
                               else if (coronIndex > 0) baseClassName.substring(0, coronIndex)
                               else ""
            if (coronIndex == baseClassName.length - 1) {
                // 末尾に ":" がつけられている場合は全てライフライン名として扱う
                lifelineName = baseClassName
                baseClassName = ""
            } else if (coronIndex > 0) {
                baseClassName = baseClassName.substring(coronIndex + 1)
            } else  {
                // ":" が無い場合は、ライフライン名として扱う
                lifelineName = baseClassName
                baseClassName = ""
            }

            // ベースクラス名としてデフォルト名を使用する
            if (baseClassName.isEmpty()) {
                baseClassName = when (participant.type) {
                    ParticipantType.ACTOR -> DEFAULT_ACTOR_NAME
                    ParticipantType.BOUNDARY -> DEFAULT_BOUNDARY_NAME
                    ParticipantType.ENTITY -> DEFAULT_ENTITY_NAME
                    ParticipantType.CONTROL -> DEFAULT_CONTROL_NAME
                    else -> ""
                }
            }
            val baseClass = if (baseClassName.isNotEmpty()) createBaseClassInstance(baseClassName.trim(), participant, isAlias)
                            else null

            // メッセージの合計数に応じてライフラインを伸ばす
            val lifeline = diagramEditor.createLifeline(lifelineName.trim(), prevX)
            if (lifeline.properties.contains("lifeline_length")) {
                lifeline.setProperty("lifeline_length", lifelineLength.toString())
            }
            if (baseClass != null) {
                (lifeline.model as ILifeline).base = baseClass
            }
            prevX += lifeline.width + X_SPAN
            Pair(participant, lifeline)
        }.toMap()
        return participantMap
    }

    private fun createBaseClassInstance(name: String, participant: Participant, isAlias: Boolean) : IClass? {
        val editorFactory = projectAccessor.modelEditorFactory
        val project = projectAccessor.project
        val elements = projectAccessor.findElements(IClass::class.java, name).filterIsInstance<IClass>()
        if (elements.isNotEmpty()) {
            return elements.first()
        }
        val baseClass = when (participant.type) {
            ParticipantType.ACTOR -> editorFactory.useCaseModelEditor.createActor(project, name)
            ParticipantType.BOUNDARY -> editorFactory.basicModelEditor.createClass(project, name).also { it.addStereotype("boundary") }
            ParticipantType.ENTITY -> editorFactory.basicModelEditor.createClass(project, name).also { it.addStereotype("entity") }
            ParticipantType.CONTROL -> editorFactory.basicModelEditor.createClass(project, name).also { it.addStereotype("control") }
            else -> if (name.isNotEmpty()) editorFactory.basicModelEditor.createClass(project, name)
                    else null
        }
        if (isAlias) {
            baseClass?.alias1 = participant.code
        }
        return baseClass
    }

    private fun convertMessage(sequenceDiagram: IDiagram?, events: List<Event>, participantMap: Map<Participant, INodePresentation>) {
        var prevMessage: ILinkPresentation? = null
        var messageCount = 0
        events.forEach { event ->
            val locY = INIT_Y + Y_SPAN * (messageCount + 1)
            if (event is Message) {
                val label = getMessageLabel(event)

                val lifeline1 = participantMap[event.participant1]
                val lifeline2 = participantMap[event.participant2]

                convertOperation(label, lifeline2?.model as ILifeline)

                val arrowConfig = event.arrowConfiguration
                val isReturn = arrowConfig.isDotted && prevMessage != null && !(prevMessage!!.model as IMessage).isAsynchronous
                val messagePresentation = createMessagePresentation(event, label, prevMessage, lifeline1, lifeline2, locY, isReturn)
                if (arrowConfig.isAsync1 || arrowConfig.isAsync2) {
                    val message = messagePresentation.model as IMessage
                    if (!message.isReturnMessage) {
                        message.isAsynchronous = true
                    }
                }

                prevMessage = messagePresentation
                messageCount++
            } else if (event is MessageExo) {
                // Frame と接続するメッセージまたは Found メッセージか Lost メッセージ

                // Found メッセージか Lost メッセージか判定する
                val isFound = event.arrowConfiguration.decoration1 == ArrowDecoration.CIRCLE
                val isLost = event.arrowConfiguration.decoration2 == ArrowDecoration.CIRCLE

                // シーケンス図の Frame の Presentation を取得する
                val framePresentation = getFramePresentation(sequenceDiagram)
                val label = getMessageLabel(event)

                val lifeline = participantMap[event.participant]

                convertOperation(label, lifeline?.model as ILifeline)

                val arrowConfig = event.arrowConfiguration
                val isReturn = arrowConfig.isDotted && prevMessage != null && !(prevMessage!!.model as IMessage).isAsynchronous
                val messagePresentation = if (isFound && isLost) {
                        // どちらの条件にも当てはまる場合は type で判断する
                        val x = when (event.type) {
                            FROM_RIGHT -> lifeline.location.x + lifeline.width + FOUND_POINT_SPAN
                            FROM_LEFT -> lifeline.location.x - FOUND_POINT_SPAN
                            TO_RIGHT -> lifeline.location.x + lifeline.width + LOST_POINT_SPAN
                            TO_LEFT -> lifeline.location.x - LOST_POINT_SPAN
                            else -> Double.NaN
                        }
                        if (x.isNaN()) {
                            null
                        } else {
                            val point = Point2D.Double(x, locY)
                            when (event.type) {
                                FROM_RIGHT, FROM_LEFT -> diagramEditor.createFoundMessage(label, point, lifeline)
                                TO_RIGHT, TO_LEFT -> diagramEditor.createLostMessage(label, lifeline, point)
                                else -> null
                            }
                        }
                    } else if (isFound) {
                        // Found メッセージ
                        val x = when (event.type) {
                            FROM_RIGHT -> lifeline.location.x + lifeline.width + FOUND_POINT_SPAN
                            FROM_LEFT -> lifeline.location.x - FOUND_POINT_SPAN
                            else -> Double.NaN
                        }
                        if (x.isNaN()) {
                            null
                        } else {
                            val point = Point2D.Double(x, locY)
                            diagramEditor.createFoundMessage(label, point, lifeline)
                        }
                    } else if (isLost) {
                        // Lost メッセージ
                        val x = when (event.type) {
                            TO_RIGHT -> lifeline.location.x + lifeline.width + LOST_POINT_SPAN
                            TO_LEFT -> lifeline.location.x - LOST_POINT_SPAN
                            else -> Double.NaN
                        }
                        if (x.isNaN()) {
                            null
                        } else {
                            val point = Point2D.Double(x, locY)
                            diagramEditor.createLostMessage(label, lifeline, point)
                        }
                    } else {
                        when (event.type) {
                            FROM_RIGHT -> createMessagePresentation(event, label, prevMessage, framePresentation, lifeline, locY, isReturn)
                            FROM_LEFT -> createMessagePresentation(event, label, prevMessage, framePresentation, lifeline, locY, isReturn)
                            TO_RIGHT -> createMessagePresentation(event, label, prevMessage, lifeline, framePresentation, locY, isReturn)
                            TO_LEFT -> createMessagePresentation(event, label, prevMessage, lifeline, framePresentation, locY, isReturn)
                            else -> null
                        }
                    }
                if (messagePresentation != null) {
                    if (arrowConfig.isAsync1 || arrowConfig.isAsync2) {
                        (messagePresentation.model as IMessage).isAsynchronous = true
                    }

                    prevMessage = messagePresentation
                    messageCount++
                }
            }
        }
    }

    private fun createMessagePresentation(message: AbstractMessage,
                                          label: String,
                                          prevMessage: ILinkPresentation?,
                                          source: INodePresentation?,
                                          target: INodePresentation?,
                                          locY: Double,
                                          isReturn: Boolean): ILinkPresentation {
        val isTargetFrame = target?.type.equals(FRAME_TYPE)
        return when {
            isReturn -> diagramEditor.createReturnMessage(label, prevMessage)
            message.isCreate && !isTargetFrame -> diagramEditor.createCreateMessage(label, source, target, locY)
            message.isDestroy && !isTargetFrame -> diagramEditor.createDestroyMessage(label, source, target, locY)
            else -> diagramEditor.createMessage(label, source, target, locY)
        }
    }

    private fun getMessageLabel(message: AbstractMessage) : String {
        val messageNumber = if (message.messageNumber.isNullOrBlank()) "" else message.messageNumber
        return when {
            message.label.isWhite -> messageNumber + "message"
            message.label.toString().isBlank() -> messageNumber + "message"
            else -> message.label.toString().replace("[\\[\\]]".toRegex(), "")
        }
    }

    private fun convertOperation(label: String, lifeLine: ILifeline) {
        if (lifeLine.base != null && lifeLine.base.operations.all { it.name != label }) {
            modelEditor.createOperation(lifeLine.base, label, "void")
        }
    }

    private fun getFramePresentation(diagram: IDiagram?): INodePresentation? {
        if (diagram == null) {
            return null
        }

        var framePresentation : INodePresentation? = null
        for (presentation in diagram.presentations) {
            if (presentation.type.equals(FRAME_TYPE)) {
                framePresentation = presentation as INodePresentation
                break
            }
        }
        return framePresentation
    }
    private class LifelineCompare : Comparator<INodePresentation> {

        override fun compare(presentation1: INodePresentation, presentation2: INodePresentation): Int {
            val x1 = presentation1.location.x
            val x2 = presentation2.location.x

            return if (x1 < x2) {
                -1
            } else if (x2 < x1) {
                1
            } else {
                0
            }
        }
    }
}