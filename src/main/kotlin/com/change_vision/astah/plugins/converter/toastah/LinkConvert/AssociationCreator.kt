package com.change_vision.astah.plugins.converter.toastah.LinkConvert

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor
import com.change_vision.astah.plugins.converter.toastah.classbody.VisibilityConverter

/**
 * 関連関係の作成を行うクラス
 */
object AssociationCreator {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * 関連を作成する
     * @param link PlantUMLのリンク
     * @param elementMap エンティティのマッピング
     * @return 作成された関連
     */
    fun createAssociation(link: Link, elementMap: Map<Entity, IClass>): INamedElement {
        // ラベルの処理
        val rawLabel = if (link.label.isWhite) "" else link.label.toString()
        val labelValue = rawLabel.replace(Regex("\\[|\\]"), "")
        
        // 関連の作成
        val association = modelEditor.createAssociation(
            elementMap[link.entity1],
            elementMap[link.entity2],
            labelValue,
            link.quantifier1, link.quantifier2
        )
        
        // 関連端の属性を設定
        val entity1Label = link.quantifier1 ?: ""
        val entity2Label = link.quantifier2 ?: ""
        
        setAssociationAttributes(association, 0, link.type.decor2)
        setAssociationAttributes(association, 1, link.type.decor1)
        
        // 多重度の処理
        processMemberEnds(association, entity1Label, entity2Label)
        
        return association
    }

    /**
     * 関連端の多重度と名前を処理する
     */
    private fun processMemberEnds(association: IAssociation, entity1Label: String, entity2Label: String) {
        try {
            MultiplicityConverter.setMultiplicityFromLabel(association.memberEnds[0], entity1Label)
            MultiplicityConverter.setMultiplicityFromLabel(association.memberEnds[1], entity2Label)
        }catch (e: Exception) {
            println("エラーが発生しました: ${e.message}")
        }
    }

    /**
     * 関連かどうかを判定する
     * @param link PlantUMLのリンク
     * @return 関連の場合true
     */
    fun isAssociation(link: Link): Boolean {
        val isNormal = link.type.style.isNormal
        val isDecor1 = associationDecors(link.type.decor1)
        val isDecor2 = associationDecors(link.type.decor2)
        
        return isNormal && (isDecor1 || isDecor2)
    }

    /**
     * 関連の装飾を判定する
     * @param decor 装飾
     * @return 関連の装飾の場合true
     */
    private fun associationDecors(decor: LinkDecor) = 
        when(decor) {
            LinkDecor.AGREGATION -> true
            LinkDecor.COMPOSITION -> true
            LinkDecor.ARROW -> true
            LinkDecor.NONE -> true
            else -> false
        }

    /**
     * 関連の属性を設定する
     * @param association 関連
     * @param endNumber エンド番号
     * @param decor 装飾
     */
    private fun setAssociationAttributes(
        association: IAssociation, 
        endNumber: Int, 
        decor: LinkDecor
    ) {
        when(decor) {
            LinkDecor.AGREGATION -> {
                association.memberEnds[endNumber].setAggregation()
            }
            LinkDecor.COMPOSITION -> {
                association.memberEnds[endNumber].setComposite()
            }
            LinkDecor.ARROW -> {
                association.memberEnds[endNumber].navigability = "Navigable"                
            }
            else -> {
                // 他の装飾は処理しない
            }
        }
    }
} 