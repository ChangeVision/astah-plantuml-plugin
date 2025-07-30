package com.change_vision.astah.plugins.converter.toastah.classbody

import com.change_vision.astah.plugins.converter.pattern.ClassPattern
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass

/**
 * 属性（フィールド）の変換
 *
 * 対象の形式:
 *   名前 : 型    (例: int : a)
 *   型 名前      (例: xxx yyy)
 *   名前のみ     (例: c)
 *
 * 処理:
 *   - 可視性を取り除く（+, -, #, ~ を検出）
 *   - : があれば、コロンの左側を型、右側を名前として扱う
 *   - コロンがない場合でスペースがあれば、スペースの左側を型、右側を名前として扱う
 *   - それ以外の場合は名前のみとして扱い、デフォルト "int" とする
 */
object AttributeConverter {

    // private val api = AstahAPI.getAstahAPI()
    // private val modelEditor = api.projectAccessor.modelEditorFactory.basicModelEditor
    
    private val api        get() = AstahAPI.getAstahAPI()
    private val modelEditor get() = api.projectAccessor.modelEditorFactory.basicModelEditor

    // デバッグログの出力制御フラグ
    private const val DEBUG = false
    
    // デバッグ出力用
    private fun println(message: String) {
        if (DEBUG) {
            kotlin.io.println("[AttributeConverter] $message")
        }
    }

    fun processAttribute(trimmed: String, aClass: IClass, modifiers: ModifiersConverter.Modifiers) {
        // 可視性記号の抽出と除去
        val visibility = if (trimmed.isNotEmpty() && trimmed.first() in listOf('+', '-', '#', '~')) {
            trimmed.first().toString()
        } else null
        
        // 可視性記号を除去したテキスト
        var text = VisibilityConverter.removeVisibility(trimmed)
        
        // 中括弧の修飾子を除去（すでにModifiersConverterで処理されているが念のため）
        text = ClassPattern.curlyBraceRegex.replace(text, "").trim()
        
        // コロンがある場合（名前:型）
        if (text.contains(":")) {
            val parts = text.split(":", limit = 2)
            val name = parts[0].trim()
            val type = parts[1].trim()
            println("コロン形式検出: 型='$type', 名前='$name'")
            createAttribute(aClass, name, type, modifiers, visibility)
            return
        }
        
        // 空白がある場合（型 名前）
        val parts = text.split(Regex("\\s+"), 2)
        if (parts.size == 2) {
            val type = parts[0].trim()
            val name = parts[1].trim()
            println("空白区切り形式: 型='$type', 名前='$name'")
            createAttribute(aClass, name, type, modifiers, visibility)
            return
        }
        
        // 名前のみの場合（デフォルト型はint）
        println("名前のみ: '$text'")
        createAttribute(aClass, text, "int", modifiers, visibility)
    }
    
    // 属性を作成
    private fun createAttribute(
        aClass: IClass, 
        name: String, 
        type: String, 
        modifiers: ModifiersConverter.Modifiers, 
        visibility: String?
        ) {
        val attr = modelEditor.createAttribute(aClass, name, type)
        
        // 可視性の設定
        if (visibility != null) {
            val visibilityString = VisibilityConverter.mapVisibility(visibility)
            attr.setVisibility(visibilityString)
            println("可視性を設定: '$visibility' -> '$visibilityString'")
        }
        
        // 修飾子の適用
        ModifiersConverter.applyModifiersForAttribute(attr, modifiers)
    }
}
