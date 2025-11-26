package com.ifpr.androidapptemplate.ui.imovel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel

class ImovelListFragment : Fragment(), ImovelAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImovelAdapter
    private var imoveisList: MutableList<Imovel> = mutableListOf()
    private var allImoveisList: MutableList<Imovel> = mutableListOf()

    private var currentModalidadeFilter: String = "Todos"
    private var currentTipoFilter: String = "Todos os tipos"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_imovel_list, container, false)

        recyclerView = view.findViewById(R.id.rvImoveis)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ImovelAdapter(imoveisList, this)
        recyclerView.adapter = adapter

        setupFilterListeners(view)
        fetchImoveis()

        return view
    }

    private fun setupFilterListeners(view: View) {
        val chipGroupModalidade = view.findViewById<ChipGroup>(R.id.chip_group_modalidade)
        chipGroupModalidade.setOnCheckedChangeListener { _, checkedId ->
            currentModalidadeFilter = when (checkedId) {
                R.id.chip_venda -> "Venda"
                R.id.chip_aluguel -> "Aluguel"
                else -> "Todos"
            }
            applyFilters()
        }

        val chipGroupTipo = view.findViewById<ChipGroup>(R.id.chip_group_tipo)
        chipGroupTipo.setOnCheckedChangeListener { _, checkedId ->
            currentTipoFilter = when (checkedId) {
                R.id.chip_casa -> "Casa"
                R.id.chip_apartamento -> "Apartamento"
                R.id.chip_condominio -> "Condomínio"
                else -> "Todos os tipos"
            }
            applyFilters()
        }
    }

    private fun fetchImoveis() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("destaques")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allImoveisList.clear()
                for (imovelSnapshot in snapshot.children) {
                    val imovel = imovelSnapshot.getValue(Imovel::class.java)
                    if (imovel != null) {
                        allImoveisList.add(imovel)
                    }
                }
                applyFilters() // Aplicar filtros iniciais
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erro
            }
        })
    }

    private fun applyFilters() {
        var filteredList: List<Imovel> = allImoveisList

        // Filtrar por modalidade
        if (currentModalidadeFilter != "Todos") {
            filteredList = filteredList.filter { it.modalidade == currentModalidadeFilter }
        }

        // Filtrar por tipo
        if (currentTipoFilter != "Todos os tipos") {
            filteredList = filteredList.filter { it.tipo == currentTipoFilter }
        }

        imoveisList.clear()
        imoveisList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(imovel: Imovel) {
        val intent = Intent(requireContext(), ImovelDetailActivity::class.java)
        intent.putExtra("IMOVEL_EXTRA", imovel)
        startActivity(intent)
    }
}
