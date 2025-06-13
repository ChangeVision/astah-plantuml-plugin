package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IPackage
import net.sourceforge.plantuml.abel.Entity

/**
 * パッケージ( IPackage ) を生成または取得する機能をまとめたクラス
 */
object PackageCreator {

    private val api = AstahAPI.getAstahAPI()
    private val modelEditor = api.projectAccessor.modelEditorFactory.basicModelEditor
    private val project = api.projectAccessor.project

    /**
     * パッケージを必要に応じて作成し返す
     */
    private val packageMap = mutableMapOf<String, IPackage>()

    fun createPackageIfNeeded(entity: Entity): IPackage {
        val qualifiers = getQualifiersList(entity)
        if (qualifiers.isEmpty()) return project

        for (index in 0 until qualifiers.size) {
            val fqn = qualifiers[index].name

            if (!packageMap.containsKey(fqn)) {
                val astahPackage = if (index==0) {
                    modelEditor.createPackage(project, qualifiers[index].name)
                }else {
                    val parent = packageMap[qualifiers[index - 1].name]
                    modelEditor.createPackage(parent, qualifiers[index].name)
                }

                if (astahPackage != null) {
                    packageMap[fqn] = astahPackage
                }
            }
        }
        return packageMap[qualifiers[qualifiers.size - 1].name]!!
    }

    private fun getQualifiersList(entity: Entity): MutableList<Entity> {
        var current: Entity? = if (entity.parentContainer.isRoot) null else entity.parentContainer
        val parentStack = mutableListOf<Entity>();
        while (current != null) {
            parentStack.add(0, current)
            current = if (current.parentContainer.isRoot) null else current.parentContainer
        }
        return parentStack
    }

}
