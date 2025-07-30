package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Success
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Failure
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import net.sourceforge.plantuml.abel.Entity

/**
 * クラス要素の変換処理を2段階に分けて行うクラス。
 *
 * 第一段階で、すべてのPlantUML要素に対して仮要素（最低限の型情報のみ）を登録し、
 * 第二段階でそれらの仮要素に詳細情報（クラスボディやステレオタイプ）を適用する。
 *
 * ※各 Leaf ごとに個別のトランザクションを用いることで、一部の変換に失敗しても
 *    他の要素の変換が継続できるようにしています。
 */
object ClassConverter {

    private val api = AstahAPI.getAstahAPI()

    /**
     * PlantUMLの要素からAstahのモデル要素を生成する。
     *
     * @param leaves PlantUMLの要素群
     * @param stereotypeMapping クラス名と適用すべきステレオタイプのマッピング
     * @return 変換結果のリスト（Success または Failure）
     */
    fun createAstahModelElements(
        leaves: Collection<Entity>,
        stereotypeMapping: Map<String, List<String>>
    ): List<ConvertResult> {
        // 変換開始前にマッピングをクリア
        ClassElement.clearMapping()
        
        // 第一段階: 仮要素登録（基本要素の作成）
        val basicErrors = mutableListOf<ConvertResult>()
        leaves.forEach { leaf ->
            try {
                TransactionManager.beginTransaction()
                ClassElement.createBasicElement(leaf)
                TransactionManager.endTransaction()
            } catch (e: Exception) {
                TransactionManager.abortTransaction()
                // エラー発生時はログとFailureとして記録
                val errorMsg = "仮要素登録エラー (${leaf.name}): ${e.message}"
                println(errorMsg)
                basicErrors.add(Failure(errorMsg))
            }
        }
        
        // 第二段階: 詳細更新
        val results = mutableListOf<ConvertResult>()
        leaves.forEach { leaf ->
            try {
                TransactionManager.beginTransaction()
                val result = ClassElement.updateElementBody(leaf, stereotypeMapping)
                TransactionManager.endTransaction()
                results.add(result)
            } catch (e: Exception) {
                TransactionManager.abortTransaction()
                val errorMsg = "詳細更新エラー (${leaf.name}): ${e.message}"
                println(errorMsg)
                results.add(Failure(errorMsg))
            }
        }
        // どちらか片方で失敗していた場合、その結果を両方まとめたい場合にはbasicErrorsも合わせて返すことが可能です。
        return basicErrors + results
    }
}
