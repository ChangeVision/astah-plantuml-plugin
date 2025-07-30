package com.change_vision.astah.plugins.converter.toastah.classstructure

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IPackage
import net.sourceforge.plantuml.abel.Entity

/**
 * パッケージ( IPackage ) を生成または取得する機能をまとめたクラス
 */
object PackageCreator {

    private val projectAccessor = AstahAPI.getAstahAPI().projectAccessor
    private val modelEditor = projectAccessor.modelEditorFactory.basicModelEditor

    fun createPackageIfNeeded(entity: Entity): IPackage {
        val qualifiers = getQualifiersList(entity)
        return createPackageHierarchyFromEntities(qualifiers)
    }

    private fun createPackageHierarchyFromEntities(entities: List<Entity>): IPackage {
        var current = projectAccessor.project as IPackage

        for (entity in entities) {
            val name = entity.name
            val found = current.ownedElements
                .filterIsInstance<IPackage>()
                .firstOrNull { it.name == name }

            current = found ?: modelEditor.createPackage(current, name)
        }

        return current
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
