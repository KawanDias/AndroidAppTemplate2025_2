package com.ifpr.androidapptemplate.baseclasses

data class Usuario(
    var key: String? = null,
    // Renomeado 'nome' para 'nomeCompleto' e adicionado 'fotoUrl'
    var nomeCompleto: String? = null,
    var email: String? = null,
    var endereco: String? = null,
    var fotoUrl: String? = null // Adicionado para exibir no marcador do mapa

)