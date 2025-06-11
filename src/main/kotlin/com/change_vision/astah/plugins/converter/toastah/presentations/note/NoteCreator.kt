package com.change_vision.astah.plugins.converter.toastah.presentations.note

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.BasicDiagramEditor
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.exception.BadTransactionException
import com.change_vision.jude.api.inf.presentation.INodePresentation
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.abel.LeafType
import java.awt.geom.Point2D

/**
 * ノート作成クラス
 */
object NoteCreator {
    private val api = AstahAPI.getAstahAPI()
    private val projectAccessor = api.projectAccessor
    private val diagramEditor = projectAccessor.diagramEditorFactory.classDiagramEditor
    private val basicDiagramEditor = diagramEditor as BasicDiagramEditor
    
    private const val DEBUG = false
    
    // ノートID -> as以降のID（toPlantUMLのためのマッピング）
    private val noteAsIdMap = mutableMapOf<String, String>()
    // ノートID -> クラスID
    private val noteToEntityMap = mutableMapOf<String, String>()
    // ノートID -> 位置
    private val notePositionMap = mutableMapOf<String, NotePosition>()
        
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[NoteCreator] $message")
        }
    }
    
    /**
     * マッピングをクリアする
     */
    fun clearMapping() {
        noteAsIdMap.clear()
        noteToEntityMap.clear()
        notePositionMap.clear()
    }
    
    /**
     * ノートIDからas以降のIDを取得する
     * @param noteId ノートID
     * @return as以降のID（存在しない場合はnull）
     */
    fun getNoteAsId(noteId: String): String? {
        // まず直接マッピングを確認
        var result = noteAsIdMap[noteId]
        if (result != null) {
            debug("直接マッピングからAsNoteIDを取得: $noteId -> $result")
            return result
        }        
        debug("AsNoteIDが見つかりません: $noteId")
        return null
    }
    
    /**
     * ノートIDから位置情報を取得する
     * @param noteId ノートID
     * @return 位置情報（存在しない場合はRIGHT）
     */
    fun getNotePosition(noteId: String): NotePosition {
        val position = notePositionMap[noteId]
        if (position != null) {
            debug("ノート位置を取得: $noteId -> $position")
            return position
        }
        debug("ノート位置が見つかりません。デフォルト位置(RIGHT)を返します: $noteId")
        return NotePosition.RIGHT
    }
    
    /**
     * ノートを作成する
     * @param diagram PlantUMLのクラス図
     * @param modelPresentationMap エンティティIDとノードプレゼンテーションのマッピング
     * @param noteMap ノートIDとノードプレゼンテーションのマッピング
     * @param rawSource 元のPlantUMLソース
     */
    fun createNotes(
        diagram: ClassDiagram,
        modelPresentationMap: MutableMap<String, INodePresentation>,
        noteMap: MutableMap<String, INodePresentation>,
        rawSource: String  
    ) {
        debug("ノート要素の作成を開始")
        
        // マッピングをクリア
        clearMapping()

        // NOTE型のLeafを処理
        var noteCount = 0
        diagram.leafs().forEach { leaf ->
            if (leaf.leafType == LeafType.NOTE) {
                val noteId = leaf.name.toString()
                val noteText = leaf.display.toString().replace(Regex("\\[|\\]"), "")
                
                debug("ノート検出: ID=$noteId, テキスト='${noteText.take(20)}${if (noteText.length > 20) "..." else ""}'")
                
                // ノートの作成（仮の位置に配置）
                val note = basicDiagramEditor.createNote(noteText, Point2D.Float(100f, 100f))
                noteMap[noteId] = note                
                noteCount++
                
                debug("ノート作成完了: 初期位置=(100,100)")
            }
        }
        
        debug("ノート要素の作成完了: 合計$noteCount 個のノートを作成")
        
        // ノートの接続情報を処理
        processNoteConnections(diagram, modelPresentationMap, noteMap, rawSource)
    }
    
    /**
     * ノート接続情報を処理する
     * @param diagram PlantUMLのクラス図
     * @param modelPresentationMap エンティティIDとノードプレゼンテーションのマッピング
     * @param noteMap ノートIDとノードプレゼンテーションのマッピング
     * @param rawSource 元のPlantUMLソース
     */
    private fun processNoteConnections(
        diagram: ClassDiagram,
        modelPresentationMap: Map<String, INodePresentation>,
        noteMap: MutableMap<String, INodePresentation>,
        rawSource: String
    ) {
        debug("ノート接続情報の処理を開始: ノート数=${noteMap.size}, エンティティ数=${modelPresentationMap.size}")
        
        // // マッピングをクリア
        // noteToEntityMap.clear()
        // notePositionMap.clear()

        // リンク情報からノートとクラスの関連を抽出
        diagram.links.forEach { link ->
            val entity1 = link.entity1?.name
            val entity2 = link.entity2?.name
            
            if (entity1 != null && entity2 != null) {
                // 片方がノート、もう片方がクラスの場合
                if (noteMap.containsKey(entity1) && modelPresentationMap.containsKey(entity2)) {
                    noteToEntityMap[entity1] = entity2
                    debug("ノート関連を抽出: ノート[$entity1] -> クラス[$entity2]")
                } else if (noteMap.containsKey(entity2) && modelPresentationMap.containsKey(entity1)) {
                    noteToEntityMap[entity2] = entity1
                    debug("ノート関連を抽出: ノート[$entity2] -> クラス[$entity1]")
                }
            }
        }
        
        // PlantUMLのソースコードからノート情報を解析
        try {
            // ソース全体を取得して解析
            if (rawSource.isNotBlank()) {
                // 一時的なnoteAsIdMapを作成
                val tempNoteAsIdMap = mutableMapOf<String, String>()
                
                NoteParsers.parseNoteInfos(
                    rawSource,
                    noteMap,
                    noteToEntityMap,
                    notePositionMap,
                    modelPresentationMap,
                    tempNoteAsIdMap
                )
                
                // クラスフィールドにtempNoteAsIdMapをマージ
                tempNoteAsIdMap.forEach { (noteId, asId) -> 
                    this.noteAsIdMap[noteId] = asId
                    
                    // 対応するノードを見つけて、そのプレゼンテーションIDにも関連付ける
                    noteMap[noteId]?.let { node ->
                        if (node.id != noteId) {
                            this.noteAsIdMap[node.id] = asId
                            debug("プレゼンテーションIDにもAsIDをマッピング: ${node.id} -> $asId")
                        }
                    }
                }
                
                debug("ノートIDとasIDのマッピングを${tempNoteAsIdMap.size}件マージしました")
            }
            
            // 位置が設定されていないノートにのみデフォルト位置を設定
            noteMap.keys.forEach { noteId ->
                if (!notePositionMap.containsKey(noteId)) {
                    val relatedEntityId = noteToEntityMap[noteId]
                    if (relatedEntityId != null) {
                        // 関連があるなら RIGHT
                        notePositionMap[noteId] = NotePosition.RIGHT
                        debug("デフォルト位置設定: 関連付けノート[$noteId] -> 位置[RIGHT]")
                    } else {
                        // 関連がないなら NONE
                        notePositionMap[noteId] = NotePosition.NONE
                        debug("デフォルト位置設定: 未関連ノート[$noteId] -> 位置[NONE]")
                    }
                }
            }
            
            // プレゼンテーションIDにも位置情報のマッピングを追加
            val presentationMapping = mutableMapOf<String, NotePosition>()
            noteMap.forEach { (noteId, note) ->
                val position = notePositionMap[noteId]
                if (position != null && note.id != noteId) {
                    presentationMapping[note.id] = position
                    debug("プレゼンテーションIDにも位置情報をマッピング: ${note.id} -> $position")
                }
            }
            // マッピングをマージ
            notePositionMap.putAll(presentationMapping)
            
            // noteAsIdMapの内容をデバッグ出力
            if (this.noteAsIdMap.isNotEmpty()) {
                debug("noteAsIdMapの内容: ${this.noteAsIdMap.entries.joinToString { "${it.key}→${it.value}" }}")
            } else {
                debug("noteAsIdMapは空です")
            }
        } catch (e: Exception) {
            debug("ノート位置解析中にエラー: ${e.message}")
            e.printStackTrace()
        }
        
        // 各ノートの位置を調整
        noteMap.forEach { (noteId, note) ->
            // 関連するクラスを取得
            val relatedClassId = noteToEntityMap[noteId]
            val relatedPosition = notePositionMap[noteId] ?: NotePosition.RIGHT
            
            if (relatedClassId != null && modelPresentationMap.containsKey(relatedClassId)) {
                // 関連するクラスがある場合
                val classPresentation = modelPresentationMap[relatedClassId]!!
                
                debug("ノート[$noteId]の関連クラス: $relatedClassId, 位置指定: $relatedPosition")
                
                val notePosition = NotePositionCalculator.calculateNotePosition(
                    classPresentation, 
                    relatedPosition,
                    120.0,  // ノートの幅
                    60.0    // ノートの高さ
                )
                
                debug("ノート[$noteId]の位置調整: 位置=$relatedPosition, 座標(${notePosition.x}, ${notePosition.y})")
                note.setLocation(notePosition)
            } else {
                // 関連するクラスがない場合は適当な位置に配置
                val idx = noteId.hashCode() % 5
                val pos = Point2D.Double(200.0 + idx * 60, 200.0 + idx * 30)
                debug("未関連ノート[$noteId]をデフォルト位置に配置: (${pos.x}, ${pos.y})")
                note.setLocation(pos)
            }
        }
        
        debug("ノート接続情報の処理を完了")
    }
    
    /**
     * ノートを処理する
     * @param diagram PlantUMLのクラス図
     * @param modelPresentationMap エンティティIDとノードプレゼンテーションのマッピング
     * @param noteMap ノートIDとノードプレゼンテーションのマッピング
     */
    fun processNotes(
        diagram: ClassDiagram,
        modelPresentationMap: Map<String, INodePresentation>,
        noteMap: Map<String, INodePresentation>
    ) {
        debug("ノートのリンク処理を開始: ノート数=${noteMap.size}")
        
        TransactionManager.beginTransaction()
        try {
            // ノート間の接続を作成
            var linkCount = 0
            diagram.links.forEach { link ->
                val entity1 = link.entity1?.name
                val entity2 = link.entity2?.name
                
                debug("リンク検出: ${entity1} -> ${entity2}")
                
                // ノートとノードの接続を処理
                if (entity1 != null && entity2 != null) {
                    val source = noteMap[entity1] ?: modelPresentationMap[entity1]
                    val target = noteMap[entity2] ?: modelPresentationMap[entity2]
                    
                    if (source != null && target != null) {
                        // ノートとクラスの接続または点線の接続
                        if (noteMap.containsKey(entity1) || noteMap.containsKey(entity2)) {
                            // ノートアンカーの作成
                            val notePresentation = if (noteMap.containsKey(entity1)) source else target
                            val targetPresentation = if (noteMap.containsKey(entity1)) target else source
                            
                            debug("ノートアンカー作成: ${if (noteMap.containsKey(entity1)) entity1 else entity2} -> ${if (noteMap.containsKey(entity1)) entity2 else entity1}")
                            basicDiagramEditor.createNoteAnchor(notePresentation, targetPresentation)
                            linkCount++
                        }
                    } else {
                        debug("リンク作成失敗: ソースまたはターゲットの表示要素が見つかりません (${entity1} -> ${entity2})")
                    }
                }
            }
            
            debug("ノートリンク処理完了: ${linkCount}個のリンクを作成")
            TransactionManager.endTransaction()
        } catch (e: BadTransactionException) {
            debug("ノート処理でトランザクションエラー: ${e.message}")
            println("ノート処理でトランザクションエラー: ${e.message}")
            TransactionManager.abortTransaction()
        }
    }
} 