package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Item(
    var key: String? = null, // Chave única do Firebase

    // Detalhes do Imóvel
    var titulo: String? = null, // Ex: "Casa com 3 quartos", "Apartamento de Luxo"
    var tipo: String? = null, // Ex: "Casa", "Apartamento", "Terreno", "Comercial"
    var preco: Double? = 0.0,
    var quartos: Int? = 0,
    var banheiros: Int? = 0,
    var metragem: Double? = 0.0, // Em metros quadrados (m²)
    var endereco: String? = null, // Endereço formatado (Ex: "Rua X, 123 - Cidade")

    // Dados para o Mapa (CRUCIAIS)
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    // Imagens/Mídia
    var imageUrl: String? = null, // URL da imagem principal no Firebase Storage
    var base64Image: String? = null // Imagem em Base64 (se usar este método)
)