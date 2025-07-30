package com.change_vision.astah.plugins.converter.toplant.activitydiagram.syntax

object LegacyActivityDiagramSyntax {
    private fun arrow(): String = "-->"

    fun initialOrFinalNode(): String = "(*)"

    fun forkJoinNode(label: String, id: String?): String =
        if (id != null) "===${label}_$id===" else "===$label==="

    fun ifBlock(source: String? = null, incomingLabel: String): String = buildString {
        if (!source.isNullOrBlank()) append("$source ")
        append("if \"$incomingLabel\" then")
    }

    fun elseBlock(): String = "else"

    fun endIfBlock(): String = "endif"

    fun node(label: String, alias: String? = null): String =
        if (alias == null) "\"$label\"" else "\"$label\" as $alias"

    fun reuseId(id: String): String = id

    fun flow(source: String?, target: String, guard: String? = null): String = buildString {
        if (!source.isNullOrBlank()) append("$source ")
        append(arrow())
        if (!guard.isNullOrBlank()) append(" [${guard}]")
        append(" $target")
    }
}