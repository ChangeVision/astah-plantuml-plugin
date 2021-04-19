package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IDiagram

enum class DiagramKind {
    ClassDiagram, SequenceDiagram, StateDiagram, ActivityDiagram
}

fun createOrGetDiagram(index: Int, diagramType: DiagramKind): IDiagram? {
    val projectAccessor = AstahAPI.getAstahAPI().projectAccessor
    val diagramEditorFactory = projectAccessor.diagramEditorFactory

    val diagramName = "${diagramType.name}_$index"

    val foundDiagramList = projectAccessor.findElements(IDiagram::class.java, diagramName)
    return when {
        foundDiagramList.isNotEmpty() -> {
            foundDiagramList.first() as IDiagram
        }
        else -> {
            TransactionManager.beginTransaction()
            try {
                when (diagramType) {
                    DiagramKind.ClassDiagram -> diagramEditorFactory.classDiagramEditor.createClassDiagram(projectAccessor.project, diagramName)
                    DiagramKind.SequenceDiagram -> diagramEditorFactory.sequenceDiagramEditor.createSequenceDiagram(projectAccessor.project, diagramName)
                    DiagramKind.StateDiagram -> diagramEditorFactory.stateMachineDiagramEditor.createStatemachineDiagram(
                        projectAccessor.project,
                        diagramName
                    )
                    DiagramKind.ActivityDiagram -> diagramEditorFactory.activityDiagramEditor.createActivityDiagram(
                        projectAccessor.project,
                        diagramName
                    )
                }.also { TransactionManager.endTransaction() }
            } catch (e: BadTransactionException) {
                TransactionManager.abortTransaction()
                null
            }
        }
    }
}