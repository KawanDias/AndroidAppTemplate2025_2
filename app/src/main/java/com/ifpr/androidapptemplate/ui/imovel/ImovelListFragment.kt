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
            Imovel("Casa moderna com piscina", "R$ 1.200.000 - Curitiba - PR", R.drawable.sample_house1),
            Imovel("Apartamento no centro", "R$ 850.000 - São Paulo - SP", R.drawable.sample_house2),
            Imovel("Casa de campo aconchegante", "R$ 690.000 - Gramado - RS", R.drawable.sample_house3),
            Imovel("Cobertura de luxo", "R$ 2.450.000 - Rio de Janeiro - RJ", R.drawable.sample_house4),
            Imovel("Studio compacto e moderno", "R$ 450.000 - Florianópolis - SC", R.drawable.sample_house5)
        )

        adapter = ImovelAdapter(imoveisList)
        recyclerView.adapter = adapter

        return view
    }
}
