package com.change_vision.astah.plugins.converter.toastah.presentations.note

import com.change_vision.jude.api.inf.presentation.INodePresentation

/**
 * ノート解析クラス
 */
object NoteParsers {
    private const val DEBUG = false
    
    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[NoteParser] $message")
        }
    }
    
    /**
     * PlantUMLソースからノート情報を解析する
     */
    fun parseNoteInfos(
        source: String,
        noteMap: Map<String, INodePresentation>,
        noteToEntityMap: MutableMap<String, String>,
        notePositionMap: MutableMap<String, NotePosition>,
        modelPresentationMap: Map<String, INodePresentation>,
        noteAsIdMap: MutableMap<String, String> = mutableMapOf()
    ) {  
        debug("===== parseNoteInfos 開始 =====")
        debug("初期状態: noteMap=${noteMap.size}件, noteToEntityMap=${noteToEntityMap.size}件, notePositionMap=${notePositionMap.size}件")
        debug("modelPresentationMap=${modelPresentationMap.size}件")
        
        // 正規表現パターンを定義
        val patterns = NotePatterns
        
        // 現在処理中のクラス名を保持
        var currentClass: String? = null
        
        // まだノートIDとエンティティIDのマッピングがされていないノート
        val unmappedNotes = noteMap.filter { (id, _) -> !noteToEntityMap.containsKey(id) }
        var unmappedIndex = 0
        
        debug("未マッピングノート数: ${unmappedNotes.size}件, IDs: ${unmappedNotes.keys.joinToString()}")

        // 行ごとに処理
        source.lines().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEachIndexed
            
            debug("行${index+1}: '$line'")
            
            // クラス定義の検出
            patterns.CLASS_PATTERN.find(line)?.let { match ->
                currentClass = match.groupValues[3]
                debug("  クラス定義検出: $currentClass")
                return@forEachIndexed
            }
            
            // ① note <pos> of <entity> : <body>
            patterns.NOTE_OF_PATTERN.find(line)?.let { match ->
                processNoteOfPattern(
                    match = match,
                    noteMap = noteMap,
                    noteToEntityMap = noteToEntityMap,
                    notePositionMap = notePositionMap,
                    modelPresentationMap = modelPresentationMap,
                    unmappedNotes = unmappedNotes,
                    unmappedIndex = unmappedIndex++
                )
                return@forEachIndexed
            }
            
            // ② note "<body>" as <id>
            patterns.NOTE_AS_PATTERN.find(line)?.let { match ->
                val body = match.groupValues[1]
                val asId = match.groupValues[2]
                
                debug("  パターン2検出: ID=$asId, 本文='${body.take(20)}${if (body.length > 20) "..." else ""}'")
                
                // 浮遊ノートなのでNONE
                if (noteMap.containsKey(asId)) {
                    notePositionMap[asId] = NotePosition.NONE
                    debug("  浮遊ノート[$asId]の位置をNONEに設定")
                    
                    // noteAsIdMapにマッピングを追加（ノートID -> as以降のID）
                    noteAsIdMap[asId] = asId
                    debug("  ノートID[$asId]とasID[$asId]のマッピングを追加")
                } else {
                    // 未マッピングノートとasIdのマッピングを試みる
                    val entry = unmappedNotes.entries.elementAtOrNull(unmappedIndex++)
                    if (entry != null) {
                        val noteId = entry.key
                        noteAsIdMap[noteId] = asId
                        notePositionMap[noteId] = NotePosition.NONE
                        debug("  未マッピングノート[$noteId]とasID[$asId]のマッピングを追加")
                    } else {
                        debug("  ノートマップに[$asId]が見つからず、未マッピングノートもありません")
                    }
                }
                return@forEachIndexed
            }
            
            // ③ note <pos> : <body>
            patterns.NOTE_INLINE_PATTERN.find(line)?.let { match ->
                processNoteInlinePattern(
                    match = match,
                    currentClass = currentClass,
                    noteMap = noteMap,
                    noteToEntityMap = noteToEntityMap,
                    notePositionMap = notePositionMap,
                    unmappedNotes = unmappedNotes,
                    unmappedIndex = unmappedIndex++
                )
                return@forEachIndexed
            }
        }
        
        debug("マッピング結果: noteToEntityMap=${noteToEntityMap.size}件, notePositionMap=${notePositionMap.size}件")
        debug("noteToEntityMap内容: ${noteToEntityMap.entries.joinToString { "${it.key}→${it.value}" }}")
        debug("notePositionMap内容: ${notePositionMap.entries.joinToString { "${it.key}→${it.value}" }}")
        debug("noteAsIdMap内容: ${noteAsIdMap.entries.joinToString { "${it.key}→${it.value}" }}")
        debug("===== parseNoteInfos 終了 =====")
    }
    
    /**
     * 「note <pos> of <entity> : <body>」パターンを処理
     */
    private fun processNoteOfPattern(
        match: MatchResult,
        noteMap: Map<String, INodePresentation>,
        noteToEntityMap: MutableMap<String, String>,
        notePositionMap: MutableMap<String, NotePosition>,
        modelPresentationMap: Map<String, INodePresentation>,
        unmappedNotes: Map<String, INodePresentation>,
        unmappedIndex: Int
    ) {
        val pos = match.groupValues[1].lowercase()
        val ent = match.groupValues[3]  // ダブルクォーテーションがあっても除去済み
        val body = match.groupValues[4].trim()
        
        debug("  パターン1検出: 位置=$pos, エンティティ=$ent, 本文='${body.take(20)}${if (body.length > 20) "..." else ""}'")
        
        // エンティティIDを検索
        val entityId = findEntityIdByName(ent, modelPresentationMap)
        debug("  エンティティID検索結果: ${if (entityId != null) "見つかった: $entityId" else "見つからなかった"}")
        
        if (entityId != null) {
            // 既存ノートに位置情報を設定
            updateExistingNotePositions(
                entityId = entityId,
                position = pos,
                noteToEntityMap = noteToEntityMap,
                notePositionMap = notePositionMap
            )

            // 未割り当てノートがあれば処理
            assignUnmappedNote(
                entityId = entityId,
                position = pos,
                unmappedNotes = unmappedNotes,
                unmappedIndex = unmappedIndex,
                noteToEntityMap = noteToEntityMap,
                notePositionMap = notePositionMap,
                noteType = "パターン1"
            )
        }
    }
    
    /**
     * 「note <pos> : <body>」パターンを処理
     */
    private fun processNoteInlinePattern(
        match: MatchResult,
        currentClass: String?,
        noteMap: Map<String, INodePresentation>,
        noteToEntityMap: MutableMap<String, String>,
        notePositionMap: MutableMap<String, NotePosition>,
        unmappedNotes: Map<String, INodePresentation>,
        unmappedIndex: Int
    ) {
        val pos = match.groupValues[1].lowercase()
        val body = match.groupValues[2].trim()
        
        debug("  パターン3検出: 位置=$pos, 本文='${body.take(20)}${if (body.length > 20) "..." else ""}'")
        
        if (currentClass != null) {
            debug("  現在のクラス: $currentClass")
            
            // 既存ノートに位置情報を設定
            updateExistingNotePositions(
                entityId = currentClass,
                position = pos,
                noteToEntityMap = noteToEntityMap,
                notePositionMap = notePositionMap
            )

            // 未割り当てノートがあれば処理
            assignUnmappedNote(
                entityId = currentClass,
                position = pos,
                unmappedNotes = unmappedNotes,
                unmappedIndex = unmappedIndex,
                noteToEntityMap = noteToEntityMap,
                notePositionMap = notePositionMap,
                noteType = "パターン3"
            )
        } else {
            debug("  現在のクラスがnullです（クラス定義がまだ見つかっていない）")
        }
    }
    
    /**
     * 既に関連付けられているノートの位置を更新
     */
    private fun updateExistingNotePositions(
        entityId: String,
        position: String,
        noteToEntityMap: MutableMap<String, String>,
        notePositionMap: MutableMap<String, NotePosition>
    ) {
        val relatedNotes = noteToEntityMap.filter { it.value == entityId }.keys
        val notesToUpdate = relatedNotes.filter { !notePositionMap.containsKey(it) }
        
        debug("  関連ノート: ${relatedNotes.size}件, 位置未設定: ${notesToUpdate.size}件")
        
        notesToUpdate.forEach { noteId ->
            notePositionMap[noteId] = parsePosition(position)
            debug("  位置を設定: ノート[$noteId] ← $entityId 位置[$position]")
        }
    }
    
    /**
     * 未割り当てのノートを割り当て
     */
    private fun assignUnmappedNote(
        entityId: String,
        position: String,
        unmappedNotes: Map<String, INodePresentation>,
        unmappedIndex: Int,
        noteToEntityMap: MutableMap<String, String>,
        notePositionMap: MutableMap<String, NotePosition>,
        noteType: String
    ) {
        if (unmappedNotes.isEmpty()) {
            debug("  未マッピングノートはありません")
            return
        }
        
        val entry = unmappedNotes.entries.elementAtOrNull(unmappedIndex)
        if (entry != null) {
            val noteId = entry.key
            noteToEntityMap[noteId] = entityId
            notePositionMap[noteId] = parsePosition(position)
            debug("  未マッピングノートを関連付け: [$noteId] → $entityId, 位置[$position]")
        } else {
            debug("  未マッピングノートがなくなりました (index=$unmappedIndex)")
        }
    }
    
    /**
     * エンティティ名からエンティティIDを検索
     */
    fun findEntityIdByName(entityName: String, modelPresentationMap: Map<String, INodePresentation>): String? {
        // IDが直接一致する場合
        if (modelPresentationMap.containsKey(entityName)) {
            return entityName
        }
        
        // ラベルが一致する場合
        for ((id, presentation) in modelPresentationMap) {
            val label = presentation.label ?: ""
            if (label == entityName || label.startsWith("$entityName ")) {
                return id
            }
        }
        
        return null
    }
} 