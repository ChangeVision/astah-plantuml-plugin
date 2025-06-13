package com.change_vision.astah.plugins.converter.toastah.LinkConvert

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import net.sourceforge.plantuml.decoration.LinkDecor

/**
 * 依存関係の作成を行うクラス
 */
object DependencyCreator {
    private val modelEditor = AstahAPI.getAstahAPI().projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * 依存かどうかを判定する
     * @param link PlantUMLのリンク
     * @return 依存の場合true
     */
    fun isDependency(link: Link) =
        link.type.style.toString().contains("DASHED") 
                && (link.type.decor1 == LinkDecor.ARROW || link.type.decor2 == LinkDecor.ARROW)

    /**
     * 依存を作成する
     * @param link PlantUMLのリンク
     * @param elementMap エンティティのマッピング
     * @return 作成された依存
     */
    fun createDependency(link: Link, elementMap: Map<Entity, IClass>): INamedElement {
        val rawLabel = if (link.label.isWhite) "" else link.label.toString()
        // 余分な角括弧を削除
        val labelValue = rawLabel.replace(Regex("\\[|\\]"), "")
                
        return if (link.type.decor2 == LinkDecor.ARROW) {
            modelEditor.createDependency(
                elementMap[link.entity1],  
                elementMap[link.entity2],  
                labelValue
            )
        } else {
            modelEditor.createDependency(
                elementMap[link.entity2],  
                elementMap[link.entity1], 
                labelValue
            )
        }
    }
} 