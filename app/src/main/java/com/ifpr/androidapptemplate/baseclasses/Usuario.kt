package com.ifpr.androidapptemplate.baseclasses

data class Usuario(
    var key: String? = null,
    var nomeCompleto: String? = null,
    var email: String? = null,
    var endereco: String? = null,
    var fotoBase64: String? = null // Alterado de fotoUrl para fotoBase64
)
