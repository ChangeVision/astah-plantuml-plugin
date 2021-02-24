package com.change_vision.astah.plugins

import com.change_vision.astah.plugins.converter.DiffManager
import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.model.IDiagram
import com.change_vision.jude.api.inf.model.IEntity
import com.change_vision.jude.api.inf.project.EntityEditListener
import com.change_vision.jude.api.inf.project.ProjectEditUnit
import com.change_vision.jude.api.inf.project.ProjectEvent
import com.change_vision.jude.api.inf.project.ProjectEventListener
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

class Activator : BundleActivator, ProjectEventListener, EntityEditListener {
    private val projectAccessor = AstahAPI.getAstahAPI().projectAccessor
    override fun start(context: BundleContext) {
        projectAccessor.addProjectEventListener(this)
        projectAccessor.addEntityEditListener(this)
    }

    override fun stop(context: BundleContext) {
        projectAccessor.removeProjectEventListener(this)
        projectAccessor.removeEntityEditListener(this)
    }

    override fun projectOpened(p0: ProjectEvent?) {}

    override fun projectClosed(p0: ProjectEvent?) {}

    override fun projectChanged(p0: ProjectEvent?) {
        if (p0 == null) return
        p0.projectEditUnit.filter { it.entity is IDiagram }.forEach {
            when (it.operation) {
                ProjectEditUnit.REMOVE -> DiffManager.removeDiagram(it.entity as IDiagram)
            }
        }
    }

    override fun preDeleteEntity(p0: Array<out IEntity>?) {
    }

    override fun preRenameEntity(p0: IEntity?, p1: String?) {
    }
}