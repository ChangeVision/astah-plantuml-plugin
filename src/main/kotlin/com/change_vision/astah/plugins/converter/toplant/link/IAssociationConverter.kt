package com.change_vision.astah.plugins.converter.toplant.link

import com.change_vision.astah.plugins.converter.toplant.classdiagram.DirectionConverter
import com.change_vision.astah.plugins.converter.toplant.classdiagram.MultiplicityConverter
import com.change_vision.jude.api.inf.model.IAssociation
import com.change_vision.jude.api.inf.model.IAttribute
import com.change_vision.jude.api.inf.model.IClass
import kotlin.collections.contains

interface IAssociationConverter {
    /**
     * 方向を表す列挙型
     */
    private enum class Direction { Left, Right }

    /**
     * 関連をPlantUML形式に変換する
     * @param model 関連
     * @param sb 出力用のStringBuilder
     */
    fun convert(model: IAssociation, sb: StringBuilder, excludeTypes : Set<String> = setOf()) {
        val end1 = model.memberEnds[0]
        val end2 = model.memberEnds[1]

        if(isInValidAssociation(end1.type, end2.type, excludeTypes)) {
            return
        }

        val assocName = model.name


        // 多重度の取得
        val multi1 = MultiplicityConverter.getMultiplicityString(end1)
        val multi2 = MultiplicityConverter.getMultiplicityString(end2)


        sb.append(formatName(end1.type))

        // 多重度を追加
        if (multi1.isNotEmpty()) {
            // ダブルクォート直後の#や*の前にチルダを追加
            val escapedMulti1 = MultiplicityConverter.escapeMultiplicityText(multi1)
            sb.append(" \"$escapedMulti1\"")
        }
        sb.append(" ")

        // 集約/合成/ナビゲーション矢印を追加
        sb.append(hatConvert(end1, Direction.Left))
        sb.append("--")
        sb.append(hatConvert(end2, Direction.Right))
        sb.append(" ")

        // 多重度を追加
        if (multi2.isNotEmpty()) {
            // ダブルクォート直後の#や*の前にチルダを追加
            val escapedMulti2 = MultiplicityConverter.escapeMultiplicityText(multi2)
            sb.append("\"$escapedMulti2\" ")
        }
        sb.append(formatName(end2.type))

        if (!assocName.isNullOrEmpty()) {
            sb.append(" : ")
            sb.append(assocName)

            // 関連名の方向矢印を追加
            val directionArrow = DirectionConverter.getAssociationDirectionArrow(model)
            if (directionArrow.isNotEmpty()) {
                sb.append(" ")
                sb.append(directionArrow)
            }
        }
        sb.appendLine()
    }

    /**
     * 関連端の装飾を変換する
     * @param end 関連端
     * @param direction 方向
     * @return PlantUMLの装飾記号
     */
    private fun hatConvert(end: IAttribute, direction: Direction): String =
        when {
            end.isComposite -> "*"
            end.isAggregate -> "o"
            else -> when (end.navigability) {
                "Navigable" -> when (direction) {
                    Direction.Left -> "<"
                    Direction.Right -> ">"
                }
                "Non_Navigable" -> getNonNavigableHat()
                else -> ""
            }
        }

    fun getNonNavigableHat() : String{
        return "x"
    }

    fun isInValidAssociation(end1 : IClass, end2 : IClass, excludeModels : Set<String>) : Boolean {
        return end1.stereotypes?.firstOrNull() in excludeModels ||
                end2.stereotypes?.firstOrNull() in excludeModels
    }

    fun formatName(clazz : IClass) : String
}