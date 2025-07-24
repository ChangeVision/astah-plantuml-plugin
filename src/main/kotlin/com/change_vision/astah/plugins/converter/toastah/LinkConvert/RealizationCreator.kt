package com.change_vision.astah.plugins.converter.toastah.LinkConvert

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor
import net.sourceforge.plantuml.abel.LeafType

/**
 * 実現関係の作成を行うクラス
 */
object RealizationCreator {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * 実現かどうかを判定する
     * @param link PlantUMLのリンク
     * @return 実現の場合true
     */
    fun isRealization(link: Link) =
        link.type.style.toString().contains("DASHED") &&
        (generalizationDecors(link.type.decor1) || generalizationDecors(link.type.decor2))

    /**
     * 実現を作成する
     * @param link PlantUMLのリンク
     * @param elementMap エンティティのマッピング
     * @return 作成された実現
     */
    fun createRealization(link: Link, elementMap: Map<Entity, IClass>): INamedElement {
        val rawLabel = if (link.label.isWhite) "" else link.label.toString()
        // 余分な角括弧を削除
        val label = rawLabel.replace(Regex("\\[|\\]"), "")
            
        val entity1 = link.entity1 as Entity
        val entity2 = link.entity2 as Entity
        val isEntity1Interface = isInterface(entity1)

        return if (isPointingToInterface(link)) {
            if (isEntity1Interface) {
                modelEditor.createRealization(
                    elementMap[entity2], 
                    elementMap[entity1], 
                    label
                )
            } else {
                modelEditor.createRealization(
                    elementMap[entity1],  
                    elementMap[entity2],  
                    label
                )
            }
        } else {
            GeneralizationCreator.createGeneralization(link, elementMap)
        }
    }

    /**
     * インターフェースかどうかを判定する
     * @param entity エンティティ
     * @return インターフェースの場合true
     */
    private fun isInterface(entity: Entity): Boolean {
        return entity.leafType == LeafType.INTERFACE || entity.leafType == LeafType.CIRCLE
    }

    /**
     * インターフェースに向かっているかどうかを判定する
     * @param link PlantUMLのリンク
     * @return インターフェースに向かっている場合true
     */
    private fun isPointingToInterface(link: Link): Boolean {
        val entity1 = link.entity1 as Entity
        val entity2 = link.entity2 as Entity
        
        return when {
            generalizationDecors(link.type.decor1) && isInterface(entity2) -> true
            generalizationDecors(link.type.decor2) && isInterface(entity1) -> true
            else -> false
        }
    }

    /**
     * 継承の装飾を判定する
     * @param decor 装飾
     * @return 継承の装飾の場合true
     */
    private fun generalizationDecors(decor:LinkDecor)=
        when(decor){
            LinkDecor.ARROW_TRIANGLE->true
            LinkDecor.EXTENDS->true
            else->false
        }
} 