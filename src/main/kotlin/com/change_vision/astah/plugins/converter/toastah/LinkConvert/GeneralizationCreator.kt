package com.change_vision.astah.plugins.converter.toastah.LinkConvert

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor

/**
 * 継承関係の作成を行うクラス
 */
object GeneralizationCreator {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * 継承かどうかを判定する
     * @param link PlantUMLのリンク
     * @return 継承の場合true
     */
    fun isGeneralization(link: Link) =
        link.type.style.isNormal &&
        (generalizationDecors(link.type.decor1) || generalizationDecors(link.type.decor2))

    /**
     * 継承の装飾を判定する
     * @param decor 装飾
     * @return 継承の装飾の場合true
     */
    fun generalizationDecors(decor:LinkDecor)=
        when(decor){
            LinkDecor.ARROW_TRIANGLE->true
            LinkDecor.EXTENDS->true
            else->false
        }

    /**
     * 継承を作成する
     * @param link PlantUMLのリンク
     * @param elementMap エンティティのマッピング
     * @return 作成された継承
     */
    fun createGeneralization(link: Link, elementMap: Map<Entity, IClass>): INamedElement {
        val decorLeft = link.type.decor1
        val rawLabel = if (link.label.isWhite) "" else link.label.toString()
        // 余分な角括弧を削除
        val label = rawLabel.replace(Regex("\\[|\\]"), "")
                
        val entity1 = link.entity1 as Entity
        val entity2 = link.entity2 as Entity

        return if (generalizationDecors(decorLeft)) {
            modelEditor.createGeneralization(
                elementMap[entity1],  
                elementMap[entity2],  
                label
            )
        } else {
            modelEditor.createGeneralization(
                elementMap[entity2],  
                elementMap[entity1],  
                label
            )
        }
    }
} 