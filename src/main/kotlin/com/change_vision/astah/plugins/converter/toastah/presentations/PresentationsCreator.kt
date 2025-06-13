package com.change_vision.astah.plugins.converter.toastah.presentations

import com.change_vision.astah.plugins.converter.toastah.presentations.node.NodePresentationsCreator
import com.change_vision.astah.plugins.converter.toastah.presentations.node.InterfacePresentationsCreator
import com.change_vision.astah.plugins.converter.toastah.presentations.link.LinkPresentationsCreator
import com.change_vision.astah.plugins.converter.toastah.presentations.note.NoteCreator
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.presentation.INodePresentation
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link
import java.awt.geom.Rectangle2D

/**
 * プレゼンテーション層の作成を統括するクラス
 */
object PresentationsCreator {
    private val api = AstahAPI.getAstahAPI()
    private val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[PresentationsCreator] $message")
        }
    }
    
    /**
     * 表示要素を作成する
     * @param entityMap エンティティのマッピング
     * @param linkMap リンクのマッピング
     * @param positionMap 位置情報のマッピング
     * @param diagram PlantUMLのクラス図
     * @param rawSource 元のPlantUMLソース
     * @return プレゼンテーションマップとノートマップのペア
     */
    fun createPresentations(
        entityMap: Map<Entity, IClass>,
        linkMap: Map<Link, INamedElement>,
        positionMap: Map<String, Rectangle2D.Float>,
        diagram: ClassDiagram,
        rawSource: String  
    ): Pair<Map<String, INodePresentation>, Map<String, INodePresentation>> {
        val modelPresentationMap = mutableMapOf<String, INodePresentation>()
        val noteMap = mutableMapOf<String, INodePresentation>()
        
        TransactionManager.beginTransaction()
        try {
            // クラス用のノードプレゼンテーションの作成
            val classNodeMap = NodePresentationsCreator.createNodePresentations(entityMap, positionMap)
            
            // インターフェース用のノードプレゼンテーションの作成
            val interfaceNodeMap = InterfacePresentationsCreator.createInterfacePresentations(entityMap, positionMap)
            
            // ノードプレゼンテーションのマップを結合
            val viewElementMap: Map<Entity, INodePresentation> = classNodeMap + interfaceNodeMap
            
            // リンクプレゼンテーションの作成
            LinkPresentationsCreator.createLinkPresentations(linkMap, viewElementMap)
            
            // エンティティIDとノードプレゼンテーションのマッピングを作成
            entityMap.forEach { (leaf, _) ->
                val entityCode = leaf.name
                val presentation = viewElementMap[leaf]
                if (presentation != null) {
                    modelPresentationMap[entityCode] = presentation
                }
            }
            
            // ノート要素を検出して作成
            NoteCreator.createNotes(diagram, modelPresentationMap, noteMap, rawSource)
            
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            debug("トランザクションエラー: ${e.message}")
            TransactionManager.abortTransaction()
        }
        
        return Pair(modelPresentationMap, noteMap)
    }
} 