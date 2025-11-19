package com.ifpr.androidapptemplate.model

import com.google.firebase.database.ServerValue

data class Notification(
    val id: String? = null,
    val imovelId: String? = null,
    val title: String? = "Novo Imóvel Adicionado!",
    val content: String? = null,
    val price: String? = null,
    val timestamp: Any? = ServerValue.TIMESTAMP,
    val deletedBy: MutableMap<String, Boolean> = mutableMapOf()
) {
    // Construtor vazio para o Firebase
    constructor() : this(null, null, "Novo Imóvel Adicionado!", null, null, ServerValue.TIMESTAMP, mutableMapOf())
}
