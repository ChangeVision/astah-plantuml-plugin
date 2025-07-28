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
import com.change_vision.jude.api.inf.project.ProjectAccessor

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

    var diagramName = "${diagramType.name}_${index}_${String.format("%03d",counter)}"

    val diagramsCache = getDiagramForType(diagramType, projectAccessor).associateBy { it.name }

    while(true){
        val foundDiagrams = diagramsCache[diagramName]

        if(foundDiagrams == null){
            break
        }

        diagramName = "${diagramType.name}_${index}_${String.format("%03d",++counter)}"

        //ループ数が際限なく増加しないようにリミッターを設ける
        if(counter > 99){
            break
        }
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

private fun getDiagramForType(diagramType: DiagramKind, projectAccessor: ProjectAccessor) : List<IDiagram>{
    return when(diagramType){
        DiagramKind.ClassDiagram -> {
            projectAccessor.findElements(IClassDiagram::class.java)
                .filterIsInstance<IClassDiagram>()
        }
        DiagramKind.SequenceDiagram -> {
            projectAccessor.findElements(ISequenceDiagram::class.java)
                .filterIsInstance<ISequenceDiagram>()
        }
        DiagramKind.StateDiagram -> {
            projectAccessor.findElements(IStateMachineDiagram::class.java)
                .filterIsInstance<IStateMachineDiagram>()
        }
        DiagramKind.ActivityDiagram -> {
            projectAccessor.findElements(IActivityDiagram::class.java)
                .filterIsInstance<IActivityDiagram>()
        }
        DiagramKind.UseCaseDiagram -> {
            projectAccessor.findElements(IUseCaseDiagram::class.java)
                .filterIsInstance<IUseCaseDiagram>()
        }
    }
}

private fun setDiagramForEditor(diagram: IDiagram, diagramType: DiagramKind, factory: IDiagramEditorFactory){
    when(diagramType){
        DiagramKind.ClassDiagram -> factory.classDiagramEditor.diagram = diagram
        DiagramKind.SequenceDiagram -> factory.sequenceDiagramEditor.diagram = diagram
        DiagramKind.StateDiagram -> factory.stateMachineDiagramEditor.diagram = diagram
        DiagramKind.ActivityDiagram -> factory.activityDiagramEditor.diagram = diagram
        DiagramKind.UseCaseDiagram -> factory.useCaseDiagramEditor.diagram = diagram
    }
}
fun isTypeMatch(diagram: IDiagram, diagramType: DiagramKind) : Boolean{
    return when(diagramType){
        DiagramKind.ClassDiagram -> diagram is IClassDiagram
        DiagramKind.SequenceDiagram -> diagram is ISequenceDiagram
        DiagramKind.StateDiagram -> diagram is IStateMachineDiagram
        DiagramKind.ActivityDiagram -> diagram is IActivityDiagram
        DiagramKind.UseCaseDiagram -> diagram is IUseCaseDiagram
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