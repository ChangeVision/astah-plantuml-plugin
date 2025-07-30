package com.change_vision.astah.plugins.converter.toplant

import com.change_vision.astah.plugins.converter.toplant.classdiagram.ClassDiagramNoteConverter
import com.change_vision.astah.plugins.converter.toplant.usecasediagram.AssociationConverter
import com.change_vision.astah.plugins.converter.toplant.usecasediagram.ExtendConverter
import com.change_vision.astah.plugins.converter.toplant.usecasediagram.IncludeConverter
import com.change_vision.astah.plugins.converter.toplant.usecasediagram.RelationshipConverter
import com.change_vision.astah.plugins.converter.toplant.usecasediagram.UseCaseConverter
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IDependency
import com.change_vision.jude.api.inf.model.IExtend
import com.change_vision.jude.api.inf.model.IGeneralization
import com.change_vision.jude.api.inf.model.IInclude
import com.change_vision.jude.api.inf.model.IRealization
import com.change_vision.jude.api.inf.model.IUseCaseDiagram
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object ToPlantUseCaseDiagramConverter {
    private const val DEBUG = false

    /**
     * デバッグログ出力
     */
    private fun debug(message: String) {
        if (DEBUG) {
            println("[ToPlantUseCaseDiagramConverter] $message")
        }
    }

    /**
     * ユースケース図をPlantUML形式に変換する
     * @param diagram ユースケース図
     * @param sb 出力用のStringBuilder
     */
    fun convert(diagram: IUseCaseDiagram, sb: StringBuilder) {
        debug("ユースケース図変換開始: ${diagram.name}")

        val noteNodes = mutableListOf<INodePresentation>()
        val allLinks = mutableListOf<ILinkPresentation>()

        diagram.presentations.forEach { presentation ->
            when(presentation) {
                is INodePresentation -> {
                    val model = presentation.model

                    if (model is IClass) {
                        UseCaseConverter.convert(model, sb)
                    } else if (presentation.type == "Note" && !presentation.label.isNullOrBlank()) {
                        noteNodes.add(presentation)
                        debug("ノート要素を検出: ID=${presentation.id}, ラベル='${presentation.label}'")
                    }
                }
                is ILinkPresentation -> {
                    allLinks.add(presentation)
                    when (val model = presentation.model) {
                        is IAssociation    -> AssociationConverter.convert(model, sb)
                        is IGeneralization -> RelationshipConverter.convertGeneralization(model, sb)
                        is IRealization    -> RelationshipConverter.convertRealization(model, sb)
                        is IDependency     -> RelationshipConverter.convertDependency(model, sb)
                        is IInclude -> IncludeConverter.convert(model, sb)
                        is IExtend -> ExtendConverter.convert(model, sb)
                    }
                }
            }
        }
        debug("ノート要素検出数: ${noteNodes.size}")


        // ノート変換
        if (noteNodes.isNotEmpty()) {
            sb.appendLine()
            ClassDiagramNoteConverter.convert(noteNodes, allLinks, sb)
        }

        debug("ユースケース図変換完了: ${diagram.name}")

    }
}