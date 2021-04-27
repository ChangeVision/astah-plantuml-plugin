package com.change_vision.astah.plugins.converter.toastah

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.INamedElement
import com.change_vision.jude.api.inf.model.IPackage
import net.sourceforge.plantuml.cucadiagram.ILeaf
import net.sourceforge.plantuml.cucadiagram.LeafType
import java.util.regex.Pattern

object ClassConverter {
    private val api = AstahAPI.getAstahAPI()
    private val modelEditor = api.projectAccessor.modelEditorFactory.basicModelEditor

    /**
     * linkの種類に応じてastahのClass/Interfaceを生成し、
     * plantモデルとastahモデルのPairをConvertResultで包んで返します。
     */
    fun createAstahModelElements(leaves: Collection<ILeaf>): List<ConvertResult> {
        TransactionManager.beginTransaction()
        return try {
            leaves
                .filter { api.projectAccessor.findElements(INamedElement::class.java, it.codeGetName).isEmpty() }
                .map { createElement(it) }
                .also { TransactionManager.endTransaction() }
        } catch (e: Exception) {
            TransactionManager.abortTransaction()
            listOf(Failure("transaction error : " + e.message))
        }
    }

    // TODO パッケージ削除時、プロジェクト変更時に削除が必要。ここに持たせない方がよさそう。
    private val packageMap = mutableMapOf<String, IPackage>()
    private val project = api.projectAccessor.project
    private fun createPackageIfNeeded(entity: ILeaf): IPackage {
        return when (entity.ident.size()) {
            1 -> project
            else -> {
                for (index in 1 until entity.ident.size()) {
                    val pkgIdent = entity.ident.getPrefix(index)
                    val fqn = pkgIdent.toString()
                    if (!packageMap.containsKey(fqn)) {
                        val astahPackage = when (index) {
                            1 -> modelEditor.createPackage(project, pkgIdent.name)
                            else -> {
                                val parent = packageMap[pkgIdent.getPrefix(index - 1).toString()]
                                modelEditor.createPackage(parent, pkgIdent.name)
                            }
                        }
                        packageMap[fqn] = astahPackage
                    }
                }
                packageMap[entity.ident.getPrefix(entity.ident.size() - 1).toString()]!!
            }
        }
    }

    private fun createElement(entity: ILeaf): ConvertResult {
        val owner = createPackageIfNeeded(entity)

        val element: Any = when (entity.leafType) {
            LeafType.CLASS -> modelEditor.createClass(owner, entity.codeGetName)
                .also { iClass -> createClassBody(iClass, entity.bodier.rawBody) }
            LeafType.ABSTRACT_CLASS -> modelEditor.createClass(owner, entity.codeGetName)
                .also { iClass ->
                    iClass.isAbstract = true
                    createClassBody(iClass, entity.bodier.rawBody)
                }
            LeafType.INTERFACE -> modelEditor.createInterface(owner, entity.codeGetName)
                .also { iClass ->
                    createClassBody(iClass, entity.bodier.rawBody)
                }
            else -> Failure("unsupported Type " + entity.leafType.name + " at " + entity.codeLine)
        }

        return when (element) {
            is IClass -> Success(Pair(entity, element))
            else -> Failure("unknown error at " + entity.codeLine)
        }
    }

    private const val visibility = """(?<visibility>[-^#+])?"""
    private const val parameter = """(?<pname>\w+)(?::(?<ptype>\w+))?"""
    private const val abstractOrStatic = """(?<abstract>abstract)?(?<static>static)?"""
    private val fieldPattern1 = Pattern.compile("""$visibility\s*(?<type>\w+)?\s+(?<name>\w+)""")
    private val fieldPattern2 = Pattern.compile("""$visibility\s*(?<name>\w+)\s*(?::\s*(?<type>\w+))?""")
    private val fieldPattern3 = Pattern.compile("""\{field\}\s*(?<text>.+)""")
    private val operatorPattern1 =
        Pattern.compile("""$abstractOrStatic\s*(?<visibility>[-^#+])?(?<optype>\w+)\s+(?<opname>\w+)\(((?<name>\w+)(?::(?<type>\w+))?)?(,$parameter)*\)""")
    private val operatorPattern2 =
        Pattern.compile("""(?<visibility>[-^#+])?(?<opname>\w+)\(((?<name>\w+)(?::(?<type>\w+))?)?(,$parameter)*\)(?::(?<optype>\w+))?""")
    private val operatorPattern3 = Pattern.compile("""\{method\}\s*(?<text>.+)""")

    private fun createClassBody(aClass: IClass, bodyTexts: List<CharSequence>) {
        bodyTexts.forEach { text ->
            val fieldMatcher1 = fieldPattern1.matcher(text)
            val fieldMatcher2 = fieldPattern2.matcher(text)
            val fieldMatcher3 = fieldPattern3.matcher(text)
            val operatorMatcher1 = operatorPattern1.matcher(text)
            val operatorMatcher2 = operatorPattern2.matcher(text)
            val operatorMatcher3 = operatorPattern3.matcher(text)
            when {
                fieldMatcher1.matches() -> {
                    val type = fieldMatcher1.group("type")
                    val name = fieldMatcher1.group("name")
                    if (type != null) {
                        modelEditor.createAttribute(aClass, name, type)
                    } else {
                        modelEditor.createAttribute(aClass, name, "int")
                    }
                }
                fieldMatcher2.matches() -> {
                    val type = fieldMatcher2.group("type")
                    val name = fieldMatcher2.group("name")
                    if (type != null) {
                        modelEditor.createAttribute(aClass, name, type)
                    } else {
                        modelEditor.createAttribute(aClass, name, "int")
                    }
                }
                fieldMatcher3.matches() -> {
                    val name = fieldMatcher3.group("text")
                    modelEditor.createAttribute(aClass, name, "int")
                }

                operatorMatcher1.matches() -> {
                    val type = operatorMatcher1.group("optype")
                    val name = operatorMatcher1.group("opname")
                    if (type != null) {
                        modelEditor.createOperation(aClass, name, type)
                    } else {
                        modelEditor.createOperation(aClass, name, "void")
                    }
                }
                operatorMatcher2.matches() -> {
                    val type = operatorMatcher2.group("optype")
                    val name = operatorMatcher2.group("opname")
                    if (type != null) {
                        modelEditor.createOperation(aClass, name, type)
                    } else {
                        modelEditor.createOperation(aClass, name, "void")
                    }
                }
                operatorMatcher3.matches() -> {
                    val comment = operatorMatcher3.group("text")
                    modelEditor.createOperation(aClass, comment, "void")
                }
                else -> {
                    modelEditor.createAttribute(aClass, text.toString(), "int")
                }
            }
        }
    }
}