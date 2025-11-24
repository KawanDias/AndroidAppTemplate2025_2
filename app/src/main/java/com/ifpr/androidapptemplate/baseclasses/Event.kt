package com.ifpr.androidapptemplate.baseclasses

/**
 * Usado como um wrapper para dados que representam um evento.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Permite leitura externa, mas não escrita

    /**
     * Retorna o conteúdo e impede seu uso novamente.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Retorna o conteúdo, mesmo que já tenha sido manipulado.
     */
    fun peekContent(): T = content
}
