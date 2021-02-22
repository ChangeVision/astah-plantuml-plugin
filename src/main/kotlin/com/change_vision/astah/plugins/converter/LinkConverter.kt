package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IElement
import net.sourceforge.plantuml.cucadiagram.ILeaf
import net.sourceforge.plantuml.cucadiagram.Link
import net.sourceforge.plantuml.cucadiagram.LinkDecor
import net.sourceforge.plantuml.cucadiagram.LinkStyle

object LinkConverter {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * linkの種類に応じてastahのAssociation/Generalization/Dependencyを生成し、
     * plantモデルとastahモデルのPairをConvertResultで包んで返します。
     */
    fun createAstahLinkElements(links: Collection<Link>, entityMap: Map<ILeaf, IClass>): List<ConvertResult> {
        TransactionManager.beginTransaction()
        return try {
            links.map { link ->
                createLink(link, entityMap)
            }.also { TransactionManager.endTransaction() }
        } catch (e: Exception) {
            TransactionManager.abortTransaction()
            listOf(Failure("transaction error : "+e.message))
        }
    }

    private fun createLink(link: Link, elementMap: Map<ILeaf, IClass>): ConvertResult {
        return when {
            isAssociation(link) -> Success(Pair(link, createAssociation(link, elementMap)))
            isGeneralization(link) -> Success(Pair(link, createGeneralization(link, elementMap)))
            isDependency(link) -> Success(Pair(link, createDependency(link, elementMap)))
            else -> Failure("unsupported Type at " + link.codeLine)
        }
    }

    private fun createAssociation(link:Link, elementMap: Map<ILeaf, IClass>):IElement{
        val association = modelEditor.createAssociation(
            elementMap[link.entity1],
            elementMap[link.entity2],
            link.label.toString(), link.qualifier1, link.qualifier2)
        setAssociationAttributes(association,0, link.type.decor1)
        setAssociationAttributes(association,1, link.type.decor2)
        return association
    }

    private fun isAssociation(link:Link) =
        link.type.style == LinkStyle.NORMAL() && (
                associationDecors(link.type.decor1) || associationDecors(link.type.decor2))

    private fun associationDecors(decor:LinkDecor)=
        when(decor){
            LinkDecor.AGREGATION->true
            LinkDecor.COMPOSITION->true
            LinkDecor.ARROW->true
            LinkDecor.NONE->true
            else->false
        }

    private fun setAssociationAttributes(association:IAssociation, endNumber:Int, decor:LinkDecor){
        when(decor){
            LinkDecor.AGREGATION -> association.memberEnds[endNumber].setAggregation()
            LinkDecor.COMPOSITION -> association.memberEnds[endNumber].setComposite()
            LinkDecor.ARROW -> association.memberEnds[endNumber].navigability = "Navigable"
            else -> {/* Not care */}
        }
    }

    private fun isGeneralization(link:Link) =
        generalizationDecors(link.type.decor1)|| generalizationDecors(link.type.decor2)

    private fun generalizationDecors(decor:LinkDecor)=
        when(decor){
            LinkDecor.ARROW_TRIANGLE->true
            LinkDecor.EXTENDS->true
            else->false
        }

    private fun createGeneralization(link:Link, elementMap: Map<ILeaf, IClass>):IElement{
        return modelEditor.createGeneralization(
            elementMap[link.entity1],
            elementMap[link.entity2],
            link.label.toString()
        )
    }

    private fun isDependency(link:Link) =
        link.type.style == LinkStyle.DOTTED()
                && (link.type.decor1==LinkDecor.ARROW || link.type.decor2==LinkDecor.ARROW)

    private fun createDependency(link:Link, elementMap: Map<ILeaf, IClass>):IElement{
        return modelEditor.createDependency(
            elementMap[link.entity1],
            elementMap[link.entity2],
            link.label.toString()
        )
    }
}