package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.astah.plugins.converter.EmptyError
import com.change_vision.astah.plugins.converter.SyntaxError
import com.change_vision.astah.plugins.converter.ValidationOK
import com.change_vision.astah.plugins.converter.ValidationResult
import com.change_vision.astah.plugins.converter.toastah.*
import net.sourceforge.plantuml.SourceStringReader
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram
import net.sourceforge.plantuml.classdiagram.ClassDiagram
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram
import net.sourceforge.plantuml.error.PSystemError
import net.sourceforge.plantuml.mindmap.MindMapDiagram
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram
import net.sourceforge.plantuml.statediagram.StateDiagram

/**
 * PlantUMLの図をAstahの図に変換するクラス
 */
object ToAstahConverter {

    /**
     * PlantUMLのソースを検証する
     * @param reader PlantUMLのソースリーダー
     * @return 検証結果
     */
    fun validate(reader: SourceStringReader): ValidationResult {
        val blocks = reader.blocks
        val errors = blocks.map { it.diagram }.filterIsInstance<PSystemError>()
        return when {
            blocks.isEmpty() -> EmptyError
            errors.isNotEmpty() -> SyntaxError(errors)
            else -> ValidationOK
        }
    }

    /**
     * ダブルクォート直後のチルダを削除する
     * @param text 処理対象のテキスト
     * @return チルダを削除したテキスト
     */
    fun deleteTilde(text: String): String {
        return text.replace("\"~~", "\"~").replace("\"~#", "\"#")
    }

    /**
     * PlantUMLのテキストをAstahの図に変換する
     * @param text PlantUMLのテキスト
     */
    fun convert(text: String) {
        // ダブルクォート直後のチルダを削除
        val processedText = deleteTilde(text)

        // ステレオタイプの抽出
        val stereotypeMapping = StereotypeExtractor.extract(processedText).getOrNull() ?: emptyMap()

        val reader = SourceStringReader(processedText)
        reader.blocks.map { it.diagram }.forEachIndexed { index, diagram ->
            when (diagram) {
                is ClassDiagram -> {
                    ToAstahClassDiagramConverter.convert(diagram, reader, index, stereotypeMapping, processedText)
                }
                is SequenceDiagram -> ToAstahSequenceDiagramConverter.convert(diagram, index)
                is DescriptionDiagram -> { // UseCase, Component, Deployment
                    // TODO コンポーネント図に対応する際にユースケース図かコンポーネント図かの判定も実装すること
                    // TODO 現状ではとりあえず全てユースケース図に変換するようにする(コンポーネントは無視する)
                    ToAstahUseCaseDiagramConverter.convert(diagram, reader, index)
                }
                is StateDiagram -> ToAstahStateDiagramConverter.convert(diagram, reader, index)
                is ActivityDiagram -> ToAstahActivityDiagramConverter.convert(diagram, reader, index)
                is PSystemError -> throw IllegalArgumentException(diagram.description.toString())
                is MindMapDiagram -> throw IllegalArgumentException("unsupported diagram type")
                else -> throw IllegalArgumentException("unknown diagram type")
            }
        }
    }
}