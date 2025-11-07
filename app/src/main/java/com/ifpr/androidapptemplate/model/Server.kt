package com.ifpr.androidapptemplate.model

data class Server(
    // CORREÇÃO: Mudado de 'val id: String = ""' para 'var id: String? = null'
    // 'var' e 'String? = null' permitem a reatribuição do Firebase key.
    var id: String? = null,

    val name: String = "",
    // Campos de Status e Localização
    val status: String = "Localização Indisponível",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastUpdated: Long = 0L
)