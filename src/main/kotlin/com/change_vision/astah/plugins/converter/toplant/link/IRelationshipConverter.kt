package com.change_vision.astah.plugins.converter.toplant.link

import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IDependency
import com.change_vision.jude.api.inf.model.IGeneralization
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IRealization

interface IRelationshipConverter {
    /**
     * 継承関係をPlantUML形式に変換する
     * @param model 継承関係
     * @param sb 出力用のStringBuilder
     * @param excludeTypes 関係の始点または終点にこのステレオタイプが含まれる場合、変換をスキップする。
     */
    fun convertGeneralization(model: IGeneralization, sb: StringBuilder, excludeTypes : Set<String> = setOf()) {
        val from = model.subType
        val target = model.superType

        if (!isValidRelationship(from, target)) {
            return
        }

        sb.append(formatName(from))
        sb.append(" --|> ")
        sb.append(formatName(target))
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
        if (!isValidRelationship(from, model.supplier)) {
            return
        }

        sb.append(formatName(from))
        sb.append(" ..|> ")
        sb.append(formatName(target))
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
        if (!isValidRelationship(from, model.supplier)) {
            return
        }

        sb.append(formatName(from))
        sb.append(" ..> ")
        sb.append(formatName(target))
        if (!model.name.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(model.name)
        }
        sb.appendLine()
    }

    fun isValidRelationship(end1: INamedElement, end2: INamedElement): Boolean {
        return true
    }

    fun formatName(element : INamedElement) : String

    fun formatName(clazz : IClass) : String
}