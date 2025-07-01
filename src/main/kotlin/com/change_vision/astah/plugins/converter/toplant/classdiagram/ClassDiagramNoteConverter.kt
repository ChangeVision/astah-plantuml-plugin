package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.astah.plugins.converter.toastah.presentations.note.NoteCreator
import com.change_vision.astah.plugins.converter.toplant.node.NoteConverter
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

/**
 * ノートをクラス図のPlantUML形式に変換するクラス
 */
object ClassDiagramNoteConverter : NoteConverter {
    private const val DEBUG = false

    private fun debug(message: String) {
        if (DEBUG) kotlin.io.println("[NoteConverter] $message")
    }

    /**
     * ノートをPlantUML形式に変換する
     */
    fun convert(
        noteNodes: List<INodePresentation>,
        linkPresentations: List<ILinkPresentation>,
        sb: StringBuilder
    ) {
        debug("ノート変換を開始: ${noteNodes.size}個のノートを処理")
        if (noteNodes.isEmpty()) return debug("ノートが見つかりませんでした")

        noteNodes.forEach { node ->
            debug("  ID=${node.id}, ラベル='${node.label}', モデル=${node.model != null}, タイプ=${node.type}")
        }

        val noteConnections = findNoteConnections(noteNodes, linkPresentations)

        noteNodes.forEach { note ->
            val text = note.label.orEmpty().takeIf { it.isNotBlank() } ?: return@forEach
            val connections = noteConnections[note].orEmpty()
            val asId = NoteCreator.getNoteAsId(note.id)
            val position = NoteCreator.getNotePosition(note.id)

            debug("ノート処理: ID=${note.id}, テキスト='${text.take(20)}...', 接続数=${connections.size}, AsNoteID=$asId, 位置=$position")

            // 位置情報を文字列に変換
            val positionStr = positionToString(position)

            when {
                asId != null -> {
                    sb.appendLine("note \"${escape(text)}\" as $asId")
                    connections.forEach { target ->
                        sb.appendLine("${target.label} .. $asId")
                    }
                }
                connections.isEmpty() -> {
                    sb.appendLine("note $positionStr: ${escape(text)}")
                }
                else -> connections.forEach { target ->
                    sb.appendLine("note $positionStr of ${target.label} : ${escape(text)}")
                }
            }
        }

        outputNoteConnections(noteNodes, linkPresentations, sb)
        debug("ノート変換を完了")
    }

    /**
     * ノートとその接続先を見つける
     */
    private fun findNoteConnections(
        noteNodes: List<INodePresentation>,
        links: List<ILinkPresentation>
    ): Map<INodePresentation, List<INodePresentation>> {
        debug("ノート接続を検索...")
        val connectionsMap = mutableMapOf<INodePresentation, MutableList<INodePresentation>>()

        links.forEach { link ->
            val src = link.source
            val dst = link.target
            when {
                src in noteNodes && dst !in noteNodes ->
                    connectionsMap.getOrPut(src) { mutableListOf() }.add(dst)
                dst in noteNodes && src !in noteNodes ->
                    connectionsMap.getOrPut(dst) { mutableListOf() }.add(src)
                else -> Unit
            }
        }

        connectionsMap.forEach { (note, targets) ->
            debug("ノート(${note.id})の接続先: ${targets.size}件")
        }
        return connectionsMap
    }

    /**
     * ノート間の接続を出力する
     */
    private fun outputNoteConnections(
        noteNodes: List<INodePresentation>,
        links: List<ILinkPresentation>,
        sb: StringBuilder
    ) {
        debug("ノート間接続を出力...")
        links.forEach { link ->
            val src = link.source
            val dst = link.target
            if (src in noteNodes && dst in noteNodes) {
                val srcId = NoteCreator.getNoteAsId(src.id)
                val dstId = NoteCreator.getNoteAsId(dst.id)
                if (srcId != null && dstId != null) {
                    sb.appendLine("$srcId .. $dstId")
                    debug("ノート間接続: $srcId .. $dstId")
                }
            }
        }
    }
}