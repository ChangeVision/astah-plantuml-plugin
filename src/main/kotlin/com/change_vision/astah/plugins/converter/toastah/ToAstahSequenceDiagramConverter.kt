package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil
import net.sourceforge.plantuml.sequencediagram.*
import java.awt.geom.Point2D


object ToAstahSequenceDiagramConverter {
    private const val X_SPAN = 20.0
    private const val Y_SPAN = 80.0
    private const val INIT_Y = 50.0

    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val modelEditor = projectAccessor.modelEditorFactory.basicModelEditor
    private val diagramEditor = projectAccessor.diagramEditorFactory.sequenceDiagramEditor
    fun convert(diagram: SequenceDiagram, index: Int) {
        // create diagram
        val sequenceDiagram = createOrGetDiagram(index, DiagramKind.SequenceDiagram)

        // convert lifeline
        TransactionManager.beginTransaction()
        try {
            var prevX = 0.0
            val participantMap = diagram.participants().mapNotNull { participant ->
                val editorFactory = projectAccessor.modelEditorFactory
                val project = projectAccessor.project
                val elements = projectAccessor.findElements(IClass::class.java, participant.code).filterIsInstance<IClass>()
                var baseClass: IClass? = null
                if (elements.isEmpty()) {
                    baseClass = when (participant.type) {
                        ParticipantType.ACTOR -> editorFactory.useCaseModelEditor.createActor(
                            project,
                            participant.code
                        )

                        ParticipantType.BOUNDARY -> editorFactory.basicModelEditor.createClass(
                            project,
                            participant.code
                        ).also { it.addStereotype("boundary") }

                        ParticipantType.ENTITY -> editorFactory.basicModelEditor.createClass(
                            project,
                            participant.code
                        ).also { it.addStereotype("entity") }

                        ParticipantType.CONTROL -> editorFactory.basicModelEditor.createClass(
                            project,
                            participant.code
                        ).also { it.addStereotype("control") }

                        else -> editorFactory.basicModelEditor.createClass(
                            project,
                            participant.code
                        )
                    }
                } else {
                    baseClass = elements.first()
                }
                if (baseClass == null) {
                    val lifeline = diagramEditor.createLifeline(participant.code, prevX)
                    prevX += lifeline.width + X_SPAN
                    Pair(participant, lifeline)
                } else {
                    val lifeline = diagramEditor.createLifeline("", prevX)
                    (lifeline.model as ILifeline).base = baseClass
                    prevX += lifeline.width + X_SPAN
                    Pair(participant, lifeline)
                }
            }.toMap()

            // メッセージの合計数に応じてライフラインを伸ばす
            var totalMessageCount = 0
            diagram.events().forEach{ event ->
                if (event is Message || event is MessageExo) {
                    totalMessageCount++
                }
            }
            val lifelineLength = INIT_Y + Y_SPAN * totalMessageCount + 40
            participantMap.values.forEach{ nodePresentation ->
                if (nodePresentation.properties.contains("lifeline_length")) {
                    nodePresentation.setProperty("lifeline_length", lifelineLength.toString())
                }
            }

            // convert combined fragments
            val groupingDeque : ArrayDeque<GroupingStart> = ArrayDeque()
            var groupingOffset = 0.0
            var groupingMessageOffset = 0.0
            val elseDeque : ArrayDeque<GroupingLeaf> = ArrayDeque()
            val operandMessageMap : HashMap<Int, Int> = HashMap()
            var operandIndex = 0
            diagram.events().forEachIndexed { i, event ->
                if (event is GroupingStart) {
                    groupingDeque.addLast(event)
                    groupingOffset += 10.0
                    operandMessageMap[operandIndex] = i
                    operandIndex++
                } else if (event is GroupingLeaf) {
                    if (event.type == GroupingType.END) {
                        if (groupingDeque.size > 0) {
                            val groupingStart: GroupingStart = groupingDeque.removeLast()
                            var groupIndex: Int = diagram.events().indexOf(groupingStart) + 1

                            var x = 0.0
                            var width = 0.0
                            var messageCount = 0
                            val lifelines: ArrayList<INodePresentation> = ArrayList()
                            var isLeft = false
                            var isRight = false
                            var firstMessage: AbstractMessage? = null

                            // 複合フラグメントの位置と大きさを計算
                            while (groupIndex < i) {
                                val innerFragmentEvent: Event = diagram.events()[groupIndex]
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
                                    val lifeline: INodePresentation? = participantMap[innerFragmentEvent.participant]
                                    if (lifeline != null && !lifelines.contains(lifeline)) {
                                        lifelines.add(lifeline)
                                    }
                                    if (innerFragmentEvent.type == MessageExoType.FROM_LEFT || innerFragmentEvent.type == MessageExoType.TO_LEFT) {
                                        isLeft = true
                                    } else if (innerFragmentEvent.type == MessageExoType.FROM_RIGHT || innerFragmentEvent.type == MessageExoType.TO_RIGHT) {
                                        isRight = true
                                    }
                                    if (firstMessage == null) {
                                        firstMessage = innerFragmentEvent
                                    }
                                    messageCount++
                                }
                                groupIndex++
                            }
                            if (isLeft) {
                                x = 1.0
                            } else {
                                run loop@{
                                    lifelines.forEach { lifeline ->
                                        if (lifeline.location.x == 0.0) {
                                            x = 1.0
                                            return@loop
                                        }
                                        if (x == 0.0) {
                                            x = lifeline.location.x
                                        } else if (lifeline.location.x < x) {
                                            x = lifeline.location.x
                                        }
                                    }
                                }
                            }
                            var firstMessageIndex = 0
                            run loop@{
                                diagram.events().forEach { e ->
                                    if (e == firstMessage) {
                                        return@loop
                                    }
                                    if (e is Message || e is MessageExo) {
                                        firstMessageIndex++
                                    }
                                }
                            }
                            val y = INIT_Y + Y_SPAN * (firstMessageIndex + 1.0) - 20
                            var mostLeftLifeline : INodePresentation? = null
                            var mostRightLifeline : INodePresentation? = null
                            lifelines.forEach { lifeline ->
                                if (mostLeftLifeline != null) {
                                    if (lifeline.location.x < mostLeftLifeline!!.location.x) {
                                        mostLeftLifeline = lifeline
                                    }
                                } else {
                                    mostLeftLifeline = lifeline
                                }
                                if (mostRightLifeline != null) {
                                    if (mostRightLifeline!!.location.x < lifeline.location.x) {
                                        mostRightLifeline = lifeline
                                    }
                                } else {
                                    mostRightLifeline = lifeline
                                }
                            }
                            if (isRight && isLeft) {
                                sequenceDiagram?.presentations?.forEach { presentation ->
                                    if (!participantMap.values.contains(presentation)) {
                                        width = (presentation as INodePresentation).width - 2.0
                                    }
                                }
                            } else if (isRight) {
                                sequenceDiagram?.presentations?.forEach { presentation ->
                                    if (!participantMap.values.contains(presentation)) {
                                        width =
                                            (presentation as INodePresentation).width - (mostLeftLifeline?.location?.x
                                                ?: 0.0)
                                    }
                                }
                            } else if (isLeft) {
                                width = (mostRightLifeline?.location?.x ?: 0.0) + (mostRightLifeline?.width ?: 0.0)
                            } else {
                                width = (mostRightLifeline?.location?.x ?: 0.0) + (mostRightLifeline?.width ?: 0.0) - (mostLeftLifeline?.location?.x ?: 0.0)
                            }

                            val title: String = groupingStart.title
                            val comment: String = groupingStart.comment
                            if (groupingOffset > 0.0) {
                                groupingOffset -= 10.0
                                if (0.0 > groupingOffset) {
                                    groupingOffset = 0.0
                                }
                            }
                            val point2D: Point2D = Point2D.Double(x + groupingOffset, y)
                            width -= groupingOffset * 2
                            val height = Y_SPAN * (messageCount - 1) + 40
                            val fragmentPresentation : INodePresentation = diagramEditor.createCombinedFragment(comment, title, point2D, width, height)
                            if (elseDeque.isNotEmpty()) {
                                val model : IElement = fragmentPresentation.model
                                if (model is ICombinedFragment) {
                                    // 2個目以降のオペランドの高さを設定していく
                                    elseDeque.forEach { groupingElse ->
                                        if (groupingElse.groupingStart == groupingStart) {
                                            val elseTitle: String = groupingElse.title
                                            var elseComment: String? = groupingElse.comment
                                            if (elseComment == null) {
                                                elseComment = ""
                                            }
                                            model.addInteractionOperand(elseTitle, elseComment)
                                        }
                                    }
                                    val properties : HashMap<Any, Any> = fragmentPresentation.properties
                                    var operandPropertyIndex = 1
                                    var processedElseDeque : ArrayDeque<GroupingLeaf> = ArrayDeque()
                                    elseDeque.forEachIndexed { j, groupingElse ->
                                        if (groupingElse.groupingStart == groupingStart) {
                                            if (j == 0) {
                                                // オペランドが追加された時点で GroupingStart で始まる1個目のオペランドの高さを設定する
                                                var firstOperandMessageCount = 0
                                                var k = diagram.events().indexOf(groupingStart) + 1
                                                while (!(diagram.events()[k] is GroupingLeaf
                                                            && (diagram.events()[k] as GroupingLeaf).groupingStart == groupingStart)
                                                ) {
                                                    if (diagram.events()[k] is Message || diagram.events()[k] is MessageExo) {
                                                        firstOperandMessageCount++
                                                    }
                                                    k++
                                                }
                                                val firstOperandKey: String =
                                                    PresentationPropertyUtil.createOperandLengthKey(operandPropertyIndex)
                                                if (properties.keys.contains(firstOperandKey)) {
                                                    val value: String = (Y_SPAN * firstOperandMessageCount).toString()
                                                    fragmentPresentation.setProperty(firstOperandKey, value)
                                                }
                                                operandPropertyIndex++
                                            }
                                            var operandMessageCount = 0
                                            var l: Int = diagram.events().indexOf(groupingElse) + 1
                                            while (!(diagram.events()[l] is GroupingLeaf
                                                        && (diagram.events()[l] as GroupingLeaf).groupingStart == groupingStart)
                                            ) {
                                                if (diagram.events()[l] is Message || diagram.events()[l] is MessageExo) {
                                                    operandMessageCount++
                                                }
                                                l++
                                            }
                                            val key: String =
                                                PresentationPropertyUtil.createOperandLengthKey(operandPropertyIndex)
                                            if (properties.keys.contains(key)) {
                                                val value: String = (Y_SPAN * operandMessageCount).toString()
                                                fragmentPresentation.setProperty(key, value)
                                            }
                                            operandPropertyIndex++
                                            processedElseDeque.add(groupingElse)
                                        }
                                    }
                                    // que から処理が終わったものを削除
                                    processedElseDeque.forEach{ processedItem ->
                                        elseDeque.remove(processedItem)
                                    }
                                }
                            }
                            // 次の処理のためにリセット
//                            elseDeque.clear()
                        }
                    } else if (event.type == GroupingType.ELSE) {
                        elseDeque.add(event)
                        operandMessageMap[operandIndex] = i
                        operandIndex++
                    }
                }
            }

            // convert messages
            var prevMessage: ILinkPresentation? = null // TODO
            val messages : ArrayDeque<Message> = ArrayDeque()
            val messageMap : MutableMap<Message, ILinkPresentation> = mutableMapOf()
            var messageCount = 0
            diagram.events().forEach { event ->
                if (event is Message) {
                    val number = if (event.messageNumber.isNullOrBlank()) "" else event.messageNumber
                    val label = when {
                        event.label.isWhite -> number + "message"
                        event.label.toString().isBlank() -> number + "message"
                        else -> event.label.toString().replace("[\\[\\]]".toRegex(), "")
                    }

                    val receiver = (participantMap[event.participant2]?.model as ILifeline).base
                    if (receiver.operations.all { it.name != label }) {
                        modelEditor.createOperation(receiver, label, "void")
                    }

                    val messagePresentation =
                        when {
                            event.arrowConfiguration.isDotted
                                    && prevMessage != null
                                    && !(prevMessage!!.model as IMessage).isAsynchronous ->
                                        diagramEditor.createReturnMessage(label, prevMessage)

                            event.arrowConfiguration.isDotted && prevMessage != null -> diagramEditor.createMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                INIT_Y + Y_SPAN * (messageCount + 1)
                            )

                            event.isCreate -> diagramEditor.createCreateMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                INIT_Y + Y_SPAN * (messageCount + 1)
                            )

                            event.isDestroy -> diagramEditor.createDestroyMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                INIT_Y + Y_SPAN * (messageCount + 1))

                            else -> diagramEditor.createMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                INIT_Y + Y_SPAN * (messageCount + 1)
                            )
                        }
                    when {
//                        event.arrowConfiguration.isAsync1 || event.arrowConfiguration.isAsync2 -> (messagePresentation.model as IMessage).isAsynchronous =
//                            true
                        event.arrowConfiguration.isAsync -> {
                            val message = messagePresentation.model as IMessage
                            if (!message.isReturnMessage) {
                                message.isAsynchronous = true
                            }
                        }
                    }

                    prevMessage = messagePresentation
                    messages.add(event)
                    messageMap[event] = messagePresentation
                    messageCount++
                }
            }
        } catch (e: Exception) {
            TransactionManager.abortTransaction()
            return
        }
        TransactionManager.endTransaction()
        if (sequenceDiagram != null) {
            api.viewManager.diagramViewManager.open(sequenceDiagram)
        }
    }
}