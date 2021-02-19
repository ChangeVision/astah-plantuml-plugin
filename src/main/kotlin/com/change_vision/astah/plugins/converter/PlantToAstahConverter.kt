package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.cucadiagram.ILeaf
import net.sourceforge.plantuml.cucadiagram.Link

object PlantToAstahConverter {
    fun convert(text: String) {
        val reader = SourceStringReader(text)
        reader.blocks.map { it.diagram }.forEach { diagram ->
            when (diagram) {
                is ClassDiagram -> {
                    val leafIClassMap =
                        diagram.leafsvalues.map { leaf ->
                            Pair(leaf, createClassElement(leaf))
                        }.toMap()
                    val linkAssociationMap =
                        diagram.links.map { link ->
                            Pair(link, createAssociationElement(link, leafIClassMap))
                        }.toMap()
                }
                else -> System.err.println("Unsupported Diagram : " + diagram.javaClass.name)
            }
        }
    }

    private val projectAccessor = AstahAPI.getAstahAPI().projectAccessor
    private val modelEditor = projectAccessor.modelEditorFactory.basicModelEditor
    private fun createClassElement(leaf: ILeaf): IClass {
        TransactionManager.beginTransaction()
        val iClass = modelEditor.createClass(projectAccessor.project, leaf.codeGetName)
        TransactionManager.endTransaction()
        return iClass
    }

    private fun createAssociationElement(link: Link, leafIClassMap: Map<ILeaf, IClass>): IAssociation {
        TransactionManager.beginTransaction()
        val e1Class = leafIClassMap[link.entity1]!!
        val e2Class = leafIClassMap[link.entity2]!!
        val iAssociation = modelEditor.createAssociation(e1Class, e2Class, link.label.toString(), "", "")
        TransactionManager.endTransaction()
        return iAssociation
    }
}
