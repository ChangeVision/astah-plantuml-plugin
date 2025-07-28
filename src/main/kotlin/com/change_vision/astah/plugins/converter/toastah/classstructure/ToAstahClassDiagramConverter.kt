package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.astah.plugins.converter.toastah.DiagramKind
import com.change_vision.astah.plugins.converter.toastah.createOrGetDiagram
import com.change_vision.astah.plugins.converter.toastah.SVGEntityCollector
import com.change_vision.astah.plugins.converter.toastah.classstructure.ClassConverter.createAstahModelElements
import com.change_vision.astah.plugins.converter.toastah.classstructure.LinkConverter.createAstahLinkElements
import com.change_vision.astah.plugins.converter.toastah.presentations.PresentationsCreator
import com.change_vision.astah.plugins.converter.toastah.presentations.note.NoteCreator
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Success
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Failure
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.Link

/**
 * PlantUMLのクラス図をAstahのクラス図に変換するクラス
 */
object ToAstahClassDiagramConverter {
    private val api = AstahAPI.getAstahAPI()

    // デバッグログの出力制御フラグ
    private const val DEBUG = false
    
    // デバッグ出力用
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[ToAstahClassDiagramConverter] $message")
        }
    }

    /**
     * PlantUMLのクラス図をAstahのクラス図に変換する
     * @param diagram PlantUMLのクラス図
     * @param reader PlantUMLのソースリーダー
     * @param index 図のインデックス
     * @param stereotypeMapping クラス名とステレオタイプのマッピング
     */
    fun convert(diagram: ClassDiagram, reader: SourceStringReader, index: Int, stereotypeMapping: Map<String, List<String>>, rawSource: String) {
        
        // モデル要素の変換
        val leafConvertResults = createAstahModelElements(diagram.leafs(), stereotypeMapping)

        leafConvertResults.filterIsInstance<Failure>().forEach { failure ->
            debug("モデル要素の変換に失敗: ${failure.message}")
        }
        
        // 成功した結果のみを抽出
        val successfulResults = leafConvertResults.filterIsInstance<Success<Pair<Entity, IClass>>>()
        
        val entityMap = successfulResults.associate { it.convertPair }

        // リンク要素の変換
        val linkConvertResults = createAstahLinkElements(diagram.links, entityMap)
                
        val linkMap = linkConvertResults.
            filterIsInstance<Success<Pair<Link, INamedElement>>>()
            .associate { it.convertPair }

        // 図の作成
        val classDiagram = createOrGetDiagram(index, DiagramKind.ClassDiagram)
        
        // 表示要素の変換
        val positionMap = SVGEntityCollector.collectSvgPosition(reader, index)
        debug("SVG位置情報取得: ${positionMap.size}個のエンティティ位置")
        
        // プレゼンテーションの作成
        val (modelPresentationMap, noteMap) = PresentationsCreator.createPresentations(entityMap, linkMap, positionMap, diagram, rawSource)
        debug("プレゼンテーション作成結果: モデル要素=${modelPresentationMap.size}個, ノート=${noteMap.size}個")

        // ノートの処理
        debug("ノートの接続処理開始")
        NoteCreator.processNotes(diagram, modelPresentationMap, noteMap)

        if (classDiagram != null) {
            debug("図を開く: ${classDiagram.name}")
            api.viewManager.diagramViewManager.open(classDiagram)
        }
        
        debug("変換処理完了")
    }
}
