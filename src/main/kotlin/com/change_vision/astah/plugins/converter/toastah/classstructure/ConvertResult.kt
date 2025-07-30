package com.change_vision.astah.plugins.converter.toastah.classstructure

/**
 * 変換結果を表すクラス
 */
sealed class ConvertResult {
    /**
     * 変換成功を表すクラス
     * @property convertPair 変換結果のペア
     */
    data class Success<T>(val convertPair: T) : ConvertResult()

    /**
     * 変換失敗を表すクラス
     * @property message エラーメッセージ
     */
    data class Failure(val message: String) : ConvertResult()
}
