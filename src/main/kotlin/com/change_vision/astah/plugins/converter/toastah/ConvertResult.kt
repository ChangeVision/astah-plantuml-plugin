package com.change_vision.astah.plugins.converter.toastah

sealed class ConvertResult
data class Success<T>(val convertPair: T) : ConvertResult()
data class Failure<E>(val error: E) : ConvertResult()
