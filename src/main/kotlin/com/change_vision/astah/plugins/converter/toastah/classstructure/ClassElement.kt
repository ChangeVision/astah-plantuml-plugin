package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Success
import com.change_vision.astah.plugins.converter.toastah.classstructure.ConvertResult.Failure
import com.change_vision.astah.plugins.converter.toastah.classbody.ClassBody
import com.change_vision.astah.plugins.converter.toastah.classstructure.PackageCreator.createPackageIfNeeded
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IClass
import com.change_vision.jude.api.inf.model.IEnumeration
import net.sourceforge.plantuml.abel.Entity
import net.sourceforge.plantuml.abel.LeafType

/**
 * クラス要素の生成・更新を2段階に分けて行うクラス。
 *
 * 第一段階では、すべてのPlantUML要素に対して仮要素を登録し、
 * 第二段階で仮要素に詳細（クラスボディ・ステレオタイプ）を適用する。
 */
object ClassElement {

    private val api = AstahAPI.getAstahAPI()
    private val modelEditor = api.projectAccessor.modelEditorFactory.basicModelEditor

    // PlantUML側とAstah側の要素対応を保持するマッピング（仮要素を含む）
    private val elementMapping = mutableMapOf<String, Any>()

    /**
     * 要素マッピングをクリアする。
     */
    fun clearMapping() {
        elementMapping.clear()
    }

    /**
     * 第一段階: 仮要素の基本情報を作成して登録する。
     *
     * @param entity PlantUMLの要素
     * @param stereotypeMapping クラス名と適用すべきステレオタイプのマッピング
     *
     * 指定された要素について、最低限の型情報（クラス、抽象クラス、インターフェース、enum）を生成し、
     * elementMappingに登録する。
     */
    fun createBasicElement(entity: Entity) {
        val className = entity.name
        if (elementMapping.containsKey(className)) return

        when (entity.leafType) {
            LeafType.NOTE -> {
                // ノートは図上のみの要素なので、モデル要素は作成しない
                elementMapping[className] = entity
            }
            LeafType.ENUM -> {
                val foundEnums = api.projectAccessor.findElements(IEnumeration::class.java, className)
                    .filterIsInstance<IEnumeration>()
                if (foundEnums.isNotEmpty()) {
                    elementMapping[className] = foundEnums.first()
                } else {
                    // 同名のIClassが存在していれば削除
                    val foundClasses = api.projectAccessor.findElements(IClass::class.java, className)
                        .filterIsInstance<IClass>()
                    if (foundClasses.isNotEmpty()) {
                        modelEditor.delete(foundClasses.first())
                    }
                    val owner = createPackageIfNeeded(entity)
                    val enumeration = modelEditor.createEnumeration(owner, className)
                    elementMapping[className] = enumeration
                }
            }
            LeafType.CLASS, 
            LeafType.ABSTRACT_CLASS, 
            LeafType.INTERFACE, 
            LeafType.CIRCLE, 
            LeafType.DESCRIPTION 
            -> {
                val foundClasses = api.projectAccessor.findElements(IClass::class.java, className)
                    .filterIsInstance<IClass>()
                if (foundClasses.isNotEmpty()) {
                    elementMapping[className] = foundClasses.first()
                } else {
                    val owner = createPackageIfNeeded(entity)
                    val element = when (entity.leafType) {
                        LeafType.CLASS -> modelEditor.createClass(owner, className)
                        LeafType.ABSTRACT_CLASS -> {
                            val cls = modelEditor.createClass(owner, className)
                            cls.isAbstract = true
                            cls
                        }
                        LeafType.INTERFACE -> modelEditor.createInterface(owner, className)
                        LeafType.CIRCLE -> modelEditor.createInterface(owner, className)
                        LeafType.DESCRIPTION -> modelEditor.createInterface(owner, className)
                        else -> throw IllegalArgumentException("unsupported type: ${entity.leafType.name}")
                    }
                    elementMapping[className] = element
                }
            }
            else -> {
            }
        }
    }

    /**
     * 第二段階: 詳細更新
     *
     * 仮要素に対して、クラスボディ（メソッド・属性定義）およびステレオタイプの適用を行う。
     *
     * @param entity PlantUMLの要素
     * @param stereotypeMapping クラス名と適用すべきステレオタイプのマッピング
     * @return 変換結果（Success または Failure）
     */
    fun updateElementBody(entity: Entity, stereotypeMapping: Map<String, List<String>>): ConvertResult {
        val className = entity.name
        val element = elementMapping[className] ?: return Failure("仮要素が見つかりません: $className")

        return when (entity.leafType) {
            LeafType.NOTE -> {
                // ノートはモデル要素を作成せず、図上での処理時に変換されるため、ここでは成功として処理する
                Success(entity to entity)
            }
            LeafType.ENUM -> {
                if (element is IEnumeration) {
                    updateEnum(element, entity)
                    Success(entity to element)
                } else {
                    Failure("要素が列挙型ではありません: $className")
                }
            }
            LeafType.CLASS, 
            LeafType.ABSTRACT_CLASS, 
            LeafType.INTERFACE, 
            LeafType.CIRCLE, 
            LeafType.DESCRIPTION
            -> {
                if (element is IClass) {
                    ClassBody.createClassBody(element, entity.bodier.rawBody)
                    // ステレオタイプの適用
                    val stereotypes = stereotypeMapping[entity.name]//要動作確認
                    stereotypes?.forEach { element.addStereotype(it) }
                    Success(entity to element)
                } else {
                    Failure("要素がクラスではありません: $className")
                }
            }
            else -> Failure("未対応の型 ${entity.leafType.name} (行: ${entity.location.toString()})")
        }
    }

    /**
     * 列挙型要素のリテラル更新処理
     *
     * @param enumeration 対象の列挙型要素
     * @param entity PlantUMLの要素
     */
    private fun updateEnum(enumeration: IEnumeration, entity: Entity) {
        val body = entity.bodier?.rawBody?.joinToString("\n")
        if (body != null) {
            // 行ごとにリテラルを生成する
            body.split("\n").forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isNotEmpty()) {
                    modelEditor.createEnumerationLiteral(enumeration, trimmedLine)
                }
            }
        }
    }
}
