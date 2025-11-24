package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartilhado com a MainActivity
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    // Dummy data - replace with your actual data source
    private val allImoveis = listOf(
        Imovel("Casa moderna com piscina", "R$ 1.200.000 - Curitiba - PR", "Venda"),
        Imovel("Apartamento no centro", "R$ 2.500/mês - São Paulo - SP", "Aluguel"),
        Imovel("Chácara com rio", "R$ 800.000 - Londrina - PR", "Venda"),
        Imovel("Kitnet perto da universidade", "R$ 900/mês - Maringá - PR", "Aluguel")
    )

    private lateinit var imovelAdapter: ImovelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imovelAdapter = ImovelAdapter(allImoveis)
        binding.recyclerViewImoveis.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = imovelAdapter
        }

        // Observa o evento para mostrar o diálogo de filtro
        dashboardViewModel.showFilterDialog.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                showFilterDialog()
            }
        })
    }

    private fun showFilterDialog() {
        val filterOptions = arrayOf(getString(R.string.filter_aluguel), getString(R.string.filter_venda), getString(R.string.filter_todos))
        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar por")
            .setItems(filterOptions) { _, which ->
                val filter = when (which) {
                    0 -> "Aluguel"
                    1 -> "Venda"
                    else -> "Todos"
                }
                filterImoveis(filter)
            }
            .show()
    }

    private fun filterImoveis(filter: String) {
        val filteredList = if (filter == "Todos") {
            allImoveis
        } else {
            allImoveis.filter { it.tipo == filter }
        }
        imovelAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Simple data class for a property
data class Imovel(val titulo: String, val preco: String, val tipo: String)
