package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.IDiagramEditorFactory
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IActivityDiagram
import com.change_vision.jude.api.inf.model.IClassDiagram
import com.change_vision.jude.api.inf.model.IDiagram
import com.change_vision.jude.api.inf.model.ISequenceDiagram
import com.change_vision.jude.api.inf.model.IStateMachineDiagram
import com.change_vision.jude.api.inf.model.IUseCaseDiagram

enum class DiagramKind {
    ClassDiagram, SequenceDiagram, StateDiagram, ActivityDiagram, UseCaseDiagram
}

fun createOrGetDiagram(index: Int, diagramType: DiagramKind) : IDiagram?{
    val api = AstahAPI.getAstahAPI()
    val projectAccessor = api.projectAccessor
    val diagramEditorFactory = projectAccessor.diagramEditorFactory

    val currentDiagram = api.viewManager.diagramViewManager.currentDiagram

    if (currentDiagram != null && isEditableDiagram(currentDiagram, diagramType)) {
        setDiagramForEditor(currentDiagram, diagramType, diagramEditorFactory)
        return currentDiagram
    }

    var counter = 0

    var diagramName = "${diagramType.name}_$index${0}"

    while(true){
        // TODO findElementsの結果をキャッシュする
        val foundDiagramList = projectAccessor.findElements(IDiagram::class.java, diagramName)
            .map { it as IDiagram }.filter { isTypeMatch(it , diagramType) }

        if(foundDiagramList.isEmpty()){
            break
        }

        if(isEditableDiagram(foundDiagramList.first(), diagramType)){
            return foundDiagramList.first()
        }

        diagramName = "${diagramType.name}_$index${++counter}"
    }

    TransactionManager.beginTransaction()
    try {
        return when (diagramType) {
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
            DiagramKind.UseCaseDiagram -> diagramEditorFactory.useCaseDiagramEditor.createUseCaseDiagram(
                projectAccessor.project,
                diagramName
            )
        }.also { TransactionManager.endTransaction() }
    } catch (e: BadTransactionException) {
        e.printStackTrace()
        TransactionManager.abortTransaction()
    }

    return null
}

private fun setDiagramForEditor(diagram: IDiagram, diagramType: DiagramKind, factory: IDiagramEditorFactory){
    when(diagramType.name){
        "ClassDiagram" -> factory.classDiagramEditor.diagram = diagram
        "SequenceDiagram" -> factory.sequenceDiagramEditor.diagram = diagram
        "StateDiagram" -> factory.stateMachineDiagramEditor.diagram = diagram
        "ActivityDiagram" -> factory.activityDiagramEditor.diagram = diagram
        "UseCaseDiagram" -> factory.useCaseDiagramEditor.diagram = diagram
        else -> false
    }
}

fun isTypeMatch(diagram: IDiagram, diagramType: DiagramKind) : Boolean{
    return when(diagramType.name){
        "ClassDiagram" -> diagram is IClassDiagram
        "SequenceDiagram" -> diagram is ISequenceDiagram
        "StateDiagram" -> diagram is IStateMachineDiagram
        "ActivityDiagram" -> diagram is IActivityDiagram
        "UseCaseDiagram" -> diagram is IUseCaseDiagram
        else -> false
    }
}

fun isEditableDiagram(diagram: IDiagram, diagramType: DiagramKind) : Boolean{
    return isTypeMatch(diagram, diagramType) && isEmptyDiagram(diagram)
}

private fun isEmptyDiagram(diagram : IDiagram): Boolean{
    val presentations = diagram.presentations

    if(presentations == null){
        return false
    }

    if(presentations.isEmpty()){
        return true
    }

    if(presentations.size == 1 && presentations[0].type == "Frame"){
        return true
    }

    return false
}