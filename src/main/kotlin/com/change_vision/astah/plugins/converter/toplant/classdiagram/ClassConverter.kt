package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IEnumeration
import com.change_vision.astah.plugins.converter.toplant.node.IClassConverter

/**
 * クラス図のクラス要素を変換するクラス
 */
object ClassConverter : IClassConverter {

    /**
     * クラスをPlantUML形式に変換する
     * @param clazz クラス
     * @param sb 出力用のStringBuilder
     */
    override fun convert(clazz: IClass, sb: StringBuilder) {
        // 1) 型判定:enum / interface / abstract / class
        val type = when {
            clazz is IEnumeration -> "enum"
            clazz.hasStereotype("interface") -> determineInterfaceNotation(clazz)
            clazz.isAbstract -> "abstract"
            else -> "class"
        }

        sb.append("$type ${formatName(clazz.name)}")

        // 2) ステレオタイプから 'interface', 'enumeration' , "actor" を除外する
        val stereotypes = filterStereotypesForClass(clazz)

        // 残ったステレオタイプだけを <<>> で出力
        sb.append(convertStereotype(stereotypes))

        val fields = clazz.attributes.filter { it.association == null }
        if (fields.isNotEmpty() || clazz.operations.isNotEmpty()) {
            sb.appendLine("{")
            fields.forEach { field ->
                sb.append("  ")
                val modifiers = mutableListOf<String>()
                if (field.isStatic) modifiers.add("static")
                if (field.hasStereotype("abstract")) modifiers.add("abstract")
                if (modifiers.isNotEmpty()) {
                    sb.append("{${modifiers.joinToString(",")}} ")
                }
                sb.appendLine(visibility(field) + field.name + " : " + field.type.name)
            }
            clazz.operations.forEach { op ->
                sb.append("  ")
                val modifiers = mutableListOf<String>()
                if (op.isStatic) modifiers.add("static")
                if (op.isAbstract) modifiers.add("abstract")
                if (modifiers.isNotEmpty()) {
                    sb.append("{${modifiers.joinToString(",")}} ")
                }
                sb.appendLine(visibility(op) + op.name + "(" + params(op.parameters) + ") : " + op.returnType.name)
            }
            sb.append("}")
        }
        sb.appendLine()
    }
}