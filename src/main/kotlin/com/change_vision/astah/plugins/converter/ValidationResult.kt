package com.change_vision.astah.plugins.converter

import net.sourceforge.plantuml.error.PSystemError

sealed class ValidationResult
object ValidationOK : ValidationResult()
class SyntaxError(val errors: List<PSystemError>) : ValidationResult()
object EmptyError : ValidationResult()
