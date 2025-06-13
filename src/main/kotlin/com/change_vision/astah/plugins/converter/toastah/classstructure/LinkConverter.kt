package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor
import net.sourceforge.plantuml.abel.LeafType
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Success
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Failure
import com.change_vision.astah.plugins.converter.toastah.LinkConvert.MultiplicityConverter
import com.change_vision.astah.plugins.converter.toastah.LinkConvert.AssociationCreator
import com.change_vision.astah.plugins.converter.toastah.LinkConvert.RealizationCreator
import com.change_vision.astah.plugins.converter.toastah.LinkConvert.GeneralizationCreator
import com.change_vision.astah.plugins.converter.toastah.LinkConvert.DependencyCreator


/**
 * リンクの変換を行うクラス
 */
object LinkConverter {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * リンク要素を作成する
     * @param links PlantUMLのリンク
     * @param entityMap エンティティのマッピング
     * @return 変換結果のリスト
     */
    fun createAstahLinkElements(links: Collection<Link>, entityMap: Map<Entity, IClass>): List<ConvertResult> {
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

    /**
     * リンクを作成する
     * @param link PlantUMLのリンク
     * @param elementMap エンティティのマッピング
     * @return 変換結果
     */
    private fun createLink(link: Link, elementMap: Map<Entity, IClass>): ConvertResult {
        return when {
            DependencyCreator.isDependency(link) -> Success<Pair<Link, INamedElement>>(Pair(link, DependencyCreator.createDependency(link, elementMap)))
            RealizationCreator.isRealization(link) -> Success<Pair<Link, INamedElement>>(Pair(link, RealizationCreator.createRealization(link, elementMap)))
            GeneralizationCreator.isGeneralization(link) -> Success<Pair<Link, INamedElement>>(Pair(link, GeneralizationCreator.createGeneralization(link, elementMap)))
            AssociationCreator.isAssociation(link) -> Success<Pair<Link, INamedElement>>(Pair(link, AssociationCreator.createAssociation(link, elementMap)))
            else -> Failure("unsupported Type at " + link.codeLine)
        }
    }
}
