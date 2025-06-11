package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation
import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassConverter
import com.change_vision.astah.plugins.converter.toplant.classdiagram.AssociationConverter
import com.change_vision.astah.plugins.converter.toplant.classdiagram.RelationshipConverter
import com.change_vision.astah.plugins.converter.toplant.NoteConverter

/**
 * クラス図をPlantUML形式に変換するコンバーター
 */
object ToPlantClassDiagramConverter {
    private const val DEBUG = false

    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            kotlin.io.println("[ToPlantClassDiagramConverter] $message")
        }
    }

    /**
     * クラス図をPlantUML形式に変換する
     * @param diagram クラス図
     * @param sb 出力用のStringBuilder
     */
    fun convert(diagram: IClassDiagram, sb: StringBuilder) {
        debug("クラス図変換開始: ${diagram.name}")

        // ノート・パッケージ・リンクの収集用リスト
        val noteNodes = mutableListOf<INodePresentation>()
        val allLinks = mutableListOf<ILinkPresentation>()

        try {
            // プレゼンテーションごとに処理を一元化
            diagram.presentations.forEach { pres ->
                when (pres) {
                    is INodePresentation -> {
                        when (val model = pres.model) {
                            is IClass -> {
                                // クラス変換
                                ClassConverter.convert(model, sb)
                            }
                            is IPackage -> {
                                debug("package")
                            }
                            else -> {
                                // ノート判定
                                if (pres.type == "Note" && !pres.label.isNullOrBlank()) {
                                    noteNodes.add(pres)
                                    debug("ノート要素を検出: ID=${pres.id}, ラベル='${pres.label}'")
                                }
                            }
                        }
                    }
                    is ILinkPresentation -> {
                        allLinks.add(pres)
                        when (val model = pres.model) {
                            is IAssociation    -> AssociationConverter.convert(model, sb)
                            is IGeneralization -> RelationshipConverter.convertGeneralization(model, sb)
                            is IRealization    -> RelationshipConverter.convertRealization(model, sb)
                            is IDependency     -> RelationshipConverter.convertDependency(model, sb)
                            else               -> { /* no-op */ }
                        }
                    }
                    else -> { /* no-op */ }
                }
            }

            debug("ノート要素検出数: ${noteNodes.size}")


            // ノート変換
            if (noteNodes.isNotEmpty()) {
                sb.appendLine()
                NoteConverter.convert(noteNodes, allLinks, sb)
            }
        } catch (e: Exception) {
            debug("変換処理中にエラーが発生しました: ${e.message}")
            e.printStackTrace()
        }

        debug("クラス図変換完了: ${diagram.name}")
    }
}