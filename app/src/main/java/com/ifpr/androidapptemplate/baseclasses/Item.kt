package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Item(
    var key: String? = null,
    var userId: String? = null,

    var titulo: String? = null,
    var tipo: String? = null,
    var preco: Double? = 0.0,
    var quartos: Int? = 0,
    var banheiros: Int? = 0,
    var metragem: Double? = 0.0,
    var endereco: String? = null,

    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    var imageUrl: String? = null,
    var base64Image: String? = null
) {
    constructor() : this(
        null, null, null, null, 0.0, 0, 0, 0.0, null, 0.0, 0.0, null, null
    )
}