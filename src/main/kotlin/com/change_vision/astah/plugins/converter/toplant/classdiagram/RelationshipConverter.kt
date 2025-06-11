package com.change_vision.astah.plugins.converter.toplant.classdiagram

import com.change_vision.jude.api.inf.model.IDependency
import com.change_vision.jude.api.inf.model.IGeneralization
import com.change_vision.jude.api.inf.model.IRealization

/**
 * クラス図のリレーションシップ（継承・実現・依存）を変換するクラス
 */
object RelationshipConverter {

    /**
     * 一般化（継承）関係をPlantUML形式に変換する
     * @param model 一般化（継承）関係
     * @param sb 出力用のStringBuilder
     */
    fun convertGeneralization(model: IGeneralization, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(model.subType.name))
        sb.append(" --|> ")
        sb.append(ClassConverter.formatName(model.superType.name))
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
     */
    fun convertRealization(model: IRealization, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(model.client.name))
        sb.append(" ..|> ")
        sb.append(ClassConverter.formatName(model.supplier.name))
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
     */
    fun convertDependency(model: IDependency, sb: StringBuilder) {
        sb.append(ClassConverter.formatName(model.client.name))
        sb.append(" ..> ")
        sb.append(ClassConverter.formatName(model.supplier.name))
        if (!model.name.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(model.name)
        }
        sb.appendLine()
    }
}