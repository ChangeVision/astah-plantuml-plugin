package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IDependency
import com.change_vision.jude.api.inf.model.IGeneralization
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IRealization

/**
 * クラス図のリレーションシップ（継承・実現・依存）を変換するクラス
 */
object RelationshipConverter {

    /**
     * 一般化（継承）関係をPlantUML形式に変換する
     * @param model 一般化（継承）関係
     * @param sb 出力用のStringBuilder
     * @param excludeTypes 関係の始点または終点にこのステレオタイプが含まれる場合、変換をスキップする。
     */
    fun convertGeneralization(model: IGeneralization, sb: StringBuilder, excludeTypes : Set<String> = setOf()) {
        val from = model.subType
        val target = model.superType
        if (shouldExclude(from, target, excludeTypes)) {
            return
        }

        sb.append(ClassConverter.formatName(from))
        sb.append(" --|> ")
        sb.append(ClassConverter.formatName(target))
        if (!model.name.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(model.name)
        }
        sb.appendLine()
    }

    /**
     * 実現関係をPlantUML形式に変換する
     * @param model 実現関係
     * @param sb 出力用のStringBuilder
     * @param excludeTypes 関係の始点または終点にこのステレオタイプが含まれる場合、変換をスキップする。
     */
    fun convertRealization(model: IRealization, sb: StringBuilder, excludeTypes : Set<String> = setOf()) {
        val from = model.client
        val target = model.supplier
        if (shouldExclude(from, target, excludeTypes)) {
            return
        }

        sb.append(ClassConverter.formatName(from))
        sb.append(" ..|> ")
        sb.append(ClassConverter.formatName(target))
        if (!model.name.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(model.name)
        }
        sb.appendLine()
    }

    /**
     * 依存関係をPlantUML形式に変換する
     * @param model 依存関係
     * @param sb 出力用のStringBuilder
     * @param excludeTypes 関係の始点または終点にこのステレオタイプが含まれる場合、変換をスキップする。
     */
    fun convertDependency(model: IDependency, sb: StringBuilder, excludeTypes : Set<String> = setOf()) {
        val from = model.client
        val target = model.supplier
        if (shouldExclude(from, model.supplier, excludeTypes)) {
            return
        }

        sb.append(ClassConverter.formatName(from))
        sb.append(" ..> ")
        sb.append(ClassConverter.formatName(target))
        if (!model.name.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(model.name)
        }
        sb.appendLine()
    }

    private fun shouldExclude(from : INamedElement, target : INamedElement, excludeTypes : Set<String>) : Boolean {
        return from.stereotypes?.firstOrNull() in excludeTypes ||
                target.stereotypes?.firstOrNull() in excludeTypes
    }

}