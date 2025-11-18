package com.ifpr.androidapptemplate.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Imovel(
    var key: String = "",
    var userId: String = "",

    var titulo: String = "",
    var tipo: String = "", // Casa, Apartamento, etc.
    var modalidade: String = "", // Venda ou Aluguel
    var preco: Double = 0.0,
    var quartos: Int = 0,
    var banheiros: Int = 0,
    var metragem: Double = 0.0,
    var endereco: String = "",
    var numero: String = "",

    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    var imageUrls: List<String> = emptyList(), // Lista de URLs de imagem
    var base64Images: List<String> = emptyList(), // Lista de imagens em Base64
    var timestamp: Long = 0
) : Parcelable
