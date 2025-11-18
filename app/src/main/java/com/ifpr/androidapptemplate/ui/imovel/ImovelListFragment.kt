package com.ifpr.androidapptemplate.ui.imovel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel

class ImovelListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImovelAdapter
    private lateinit var imoveisList: List<Imovel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_imovel_list, container, false)

        recyclerView = view.findViewById(R.id.rvImoveis)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Lista simulada de imóveis disponíveis
        imoveisList = listOf(
            Imovel(titulo = "Casa moderna com piscina", preco = 1200000.0, endereco = "Curitiba - PR", quartos = 4, banheiros = 3, metragem = 300.0, tipo = "Casa"),
            Imovel(titulo = "Apartamento no centro", preco = 850000.0, endereco = "São Paulo - SP", quartos = 3, banheiros = 2, metragem = 150.0, tipo = "Apartamento"),
            Imovel(titulo = "Casa de campo aconchegante", preco = 690000.0, endereco = "Gramado - RS", quartos = 2, banheiros = 2, metragem = 180.0, tipo = "Casa"),
            Imovel(titulo = "Cobertura de luxo", preco = 2450000.0, endereco = "Rio de Janeiro - RJ", quartos = 5, banheiros = 5, metragem = 500.0, tipo = "Cobertura"),
            Imovel(titulo = "Studio compacto e moderno", preco = 450000.0, endereco = "Florianópolis - SC", quartos = 1, banheiros = 1, metragem = 50.0, tipo = "Studio")
        )

        adapter = ImovelAdapter(imoveisList)
        recyclerView.adapter = adapter

        return view
    }
}
