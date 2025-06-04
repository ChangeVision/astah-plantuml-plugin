package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.astah.plugins.converter.toplant.DiagramKind
import com.change_vision.astah.plugins.converter.toplant.createOrGetDiagram
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.ILifeline
import com.change_vision.jude.api.inf.model.IMessage
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import net.sourceforge.plantuml.sequencediagram.Message
import net.sourceforge.plantuml.sequencediagram.ParticipantType
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram


object ToAstahSequenceDiagramConverter {
    private const val xSpan = 20.0
    private const val ySpan = 40.0
    private const val initY = 50.0

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
                val baseClass: IClass? = when (participant.type) {
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
                if (baseClass == null) {
                    val lifeline = diagramEditor.createLifeline(participant.code, prevX)
                    prevX += lifeline.width + xSpan
                    Pair(participant, lifeline)
                } else {
                    val lifeline = diagramEditor.createLifeline("", prevX)
                    (lifeline.model as ILifeline).base = baseClass
                    prevX += lifeline.width + xSpan
                    Pair(participant, lifeline)
                }
            }.toMap()

            // convert messages
            var prevMessage: ILinkPresentation? = null // TODO
            diagram.events().forEachIndexed { i, event ->
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
                            event.arrowConfiguration.isDotted && prevMessage != null ->
                                diagramEditor.createReturnMessage(label, prevMessage)
                            event.isCreate -> diagramEditor.createCreateMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                initY + ySpan * (i + 1)
                            )
                            else -> diagramEditor.createMessage(
                                label,
                                participantMap[event.participant1],
                                participantMap[event.participant2],
                                initY + ySpan * (i + 1)
                            )
                        }
                    when {
                        event.arrowConfiguration.isAsync1 || event.arrowConfiguration.isAsync2 -> (messagePresentation.model as IMessage).isAsynchronous =
                            true
                    }

                    prevMessage = messagePresentation
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