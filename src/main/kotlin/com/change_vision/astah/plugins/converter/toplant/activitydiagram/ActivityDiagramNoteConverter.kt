package com.change_vision.astah.plugins.converter.toplant.activitydiagram

import com.change_vision.astah.plugins.converter.toastah.presentations.note.NoteCreator
import com.change_vision.astah.plugins.converter.toplant.node.NoteConverter
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ActivityDiagramNoteConverter: NoteConverter {
    fun convert(note: INodePresentation): String{
        return "note ${positionToString(NoteCreator.getNotePosition(note.id))}: ${escape(note.label?: "")}"
    }

    fun getNotes(node: INodePresentation): List<INodePresentation>{
        val noteAnchors = node.links
            .filter { it.type == "NoteAnchor" }

        val results = mutableListOf<INodePresentation>()
        for (noteAnchor in noteAnchors){
            when (node) {
                noteAnchor.source -> results.add(noteAnchor.target)
                else -> results.add(noteAnchor.source)
            }
        }
        return results
    }
}