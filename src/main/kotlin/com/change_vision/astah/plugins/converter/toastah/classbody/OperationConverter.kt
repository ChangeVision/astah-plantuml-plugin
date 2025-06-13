package com.change_vision.astah.plugins.converter.toastah.classbody

import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IOperation

/**
 * 操作（メソッド）の変換
 *
 * 対象の形式:
 *   名前(引数1:型1, 引数2:型2, ...):戻り値型
 *
 * 処理:
 *   - 可視性を取り除く（+, -, #, ~ を検出）
 *   - 括弧"()"の前までをメソッド名として扱う
 *   - ":"がある場合、その右側を返り値の型として扱う、なければvoidをデフォルト値とする
 *   - 括弧内は「名前:型」形式で解析。":"がない場合は全体を名前として、型はintとする
 */
object OperationConverter {

    // private val api = AstahAPI.getAstahAPI()
    // private val modelEditor = api.projectAccessor.modelEditorFactory.basicModelEditor
    
    private val api        get() = AstahAPI.getAstahAPI()
    private val modelEditor get() = api.projectAccessor.modelEditorFactory.basicModelEditor

    
    // デバッグログの出力制御フラグ
    private const val DEBUG = false
    
    // デバッグ出力用
    private fun println(message: String) {
        if (DEBUG) {
            kotlin.io.println("[OperationConverter] $message")
        }
    }

    fun processOperation(trimmed: String, aClass: IClass, modifiers: ModifiersConverter.Modifiers) {
        // 可視性記号の抽出と除去
        val visibility = if (trimmed.isNotEmpty() && trimmed.first() in listOf('+', '-', '#', '~')) {
            trimmed.first().toString()
        } else null
        
        // 可視性記号を除去したテキスト
        var text = VisibilityConverter.removeVisibility(trimmed)
        
        // 中括弧の修飾子を除去（すでにModifiersConverterで処理されているが念のため）
        text = ClassPattern.curlyBraceRegex.replace(text, "").trim()
        
        // メソッド名の抽出（括弧の前まで）
        val openBracketIndex = text.indexOf("(")
        if (openBracketIndex == -1) {
            // 括弧がない場合は単純なメソッド名として処理
            println("括弧なし - メソッド名のみ: '$text'")
            createOperation(aClass, text, "void", "", modifiers, visibility)
            return
        }
        
        val methodName = text.substring(0, openBracketIndex).trim()
        
        // パラメータの抽出
        val closeBracketIndex = text.indexOf(")", openBracketIndex)
        if (closeBracketIndex == -1) {
            // 閉じ括弧がない場合は構文エラーだが、名前のみ処理して続行
            println("閉じ括弧なしエラー: '$text'")
            createOperation(aClass, methodName, "void", "", modifiers, visibility)
            return
        }
        
        val parametersText = text.substring(openBracketIndex + 1, closeBracketIndex).trim()
        
        // 戻り値型の抽出
        var returnType = "void" // デフォルト値
        val restText = text.substring(closeBracketIndex + 1).trim()
        
        if (restText.startsWith(":")) {
            returnType = restText.substring(1).trim()
        }
        
        println("メソッド解析: 名前='$methodName', パラメータ='$parametersText', 戻り値='$returnType'")
        
        // 操作の作成
        createOperation(aClass, methodName, returnType, parametersText, modifiers, visibility)
    }
    
    // 操作を作成
    private fun createOperation(
        aClass: IClass, 
        name: String, 
        returnType: String, 
        parametersText: String, 
        modifiers: ModifiersConverter.Modifiers, 
        visibility: String?
        ) {
        val operation = modelEditor.createOperation(aClass, name, returnType)
        
        // 可視性の設定
        if (visibility != null) {
            val visibilityString = VisibilityConverter.mapVisibility(visibility)
            operation.setVisibility(visibilityString)
            println("可視性を設定: '$visibility' -> '$visibilityString'")
        }
        
        // 修飾子の適用
        ModifiersConverter.applyModifiersForOperation(operation, modifiers)
        
        // パラメータの処理
        createParameters(operation, parametersText)
    }
    
    // パラメータを作成
    private fun createParameters(operation: IOperation, parametersText: String) {
        if (parametersText.isEmpty()) return
        
        parametersText.split(",").map { it.trim() }.forEach { paramText ->
            if (paramText.isEmpty()) return@forEach
            
            // パラメータ名と型の分離
            if (paramText.contains(":")) {
                // コロン区切り形式（名前:型）
                val parts = paramText.split(":", limit = 2)
                val paramName = parts[0].trim()
                val paramType = parts[1].trim()
                
                println("パラメータ(コロン区切り): 型='$paramType', 名前='$paramName'")
                modelEditor.createParameter(operation, paramName, paramType)
            } else if (paramText.contains(" ")) {
                // 空白区切り形式（型 名前）
                val parts = paramText.split(" ", limit = 2)
                val paramType = parts[0].trim()
                val paramName = parts[1].trim()
                
                println("パラメータ(空白区切り): 名前='$paramName', 型='$paramType'")
                modelEditor.createParameter(operation, paramName, paramType)
            } else {
                // それ以外の場合はパラメータ名のみ、型はintとする
                println("パラメータ（型省略）: 名前='$paramText', 型='int'")
                modelEditor.createParameter(operation, paramText, "int")
            }
        }
    }
}
