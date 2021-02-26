package com.change_vision.astah.plugins.converter

import com.change_vision.jude.api.inf.model.*
import com.change_vision.jude.api.inf.presentation.ILinkPresentation
import com.change_vision.jude.api.inf.presentation.INodePresentation

object AstahToPlantClassDiagramConverter {
    fun convert(diagram: IClassDiagram, sb: StringBuilder) {
        diagram.presentations.forEach {
            when (it) {
                is INodePresentation -> when (val model = it.model) {
                    is IClass -> classConvert(model, sb)
                    else -> null
                }
                is ILinkPresentation -> when (val model = it.model) {
                    is IAssociation -> association(model, sb)
                    is IGeneralization -> generalization(model, sb)
                    is IDependency -> dependency(model, sb)
                    else -> null
                }
                else -> null
            }
        }
    }

    private fun classConvert(clazz: IClass, sb: StringBuilder) {
        val type = when {
            clazz.hasStereotype("interface") -> "interface"
            clazz.isAbstract -> "abstract"
            else -> "class"
        }
        sb.append(type + " " + clazz.name)
        val fields = clazz.attributes.filter { it.association == null }
        if (fields.isNotEmpty() || clazz.operations.isNotEmpty()) {
            sb.appendLine("{")
            fields.forEach { field ->
                sb.append("  ")
                sb.appendLine(visibility(field) + field.name + " : " + field.type.name)
            }
            clazz.operations.forEach { op ->
                sb.append("  ")
                sb.appendLine(visibility(op) + op.name + "(" + params(op.parameters) + ") : " + op.returnType.name)
            }
            sb.append("}")
        }
        sb.appendLine()
    }

    private fun visibility(element: INamedElement) =
        when {
            element.isPrivateVisibility -> "-"
            element.isProtectedVisibility -> "#"
            element.isPackageVisibility -> "~"
            element.isPublicVisibility -> "+"
            else -> ""
        }

    private fun params(params: Array<IParameter>) =
        params.map { it.name + ":" + it.type.name }.joinToString { "," }


    private enum class Direction { Left, Right }

    private fun association(model: IAssociation, sb: StringBuilder) {
        val end1 = model.memberEnds[0]
        val end2 = model.memberEnds[1]
        sb.append(end1.type.name)
        sb.append(" ")
        sb.append(hatConvert(end1, Direction.Left))
        sb.append("--")
        sb.append(hatConvert(end2, Direction.Right))
        sb.append(" ")
        sb.append(end2.type.name)
        sb.appendLine()
    }

    private fun hatConvert(end: IAttribute, direction: Direction) =
        when {
            end.isComposite -> "*"
            end.isAggregate -> "o"
            else -> when (end.navigability) {
                "Navigable" -> when (direction) {
                    Direction.Left -> "<"
                    Direction.Right -> ">"
                }
                "Non_Navigabl" -> "x"
                else -> ""
            }
        }

    private fun generalization(model: IGeneralization, sb: StringBuilder) {
        sb.append(model.subType.name)
        sb.append(" --|> ")
        sb.appendLine(model.superType.name)
    }

    private fun dependency(model: IDependency, sb: StringBuilder) {
        sb.append(model.client.name)
        sb.append(" ..> ")
        sb.appendLine(model.supplier.name)
    }
}