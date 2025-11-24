package com.ifpr.androidapptemplate.ui.imovel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        setupMenu()
        fetchImoveis()

        return view
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.imovel_list_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_filter) {
                    val anchorView = requireActivity().findViewById<View>(R.id.action_filter)
                    showFilterMenu(anchorView)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showFilterMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.filter_todos -> filterImoveis("Todos")
                R.id.filter_aluguel -> filterImoveis("Aluguel")
                R.id.filter_venda -> filterImoveis("Venda")
            }
            true
        }

        popup.show()
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
                filterImoveis("Todos") // Exibe todos por padrão
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erro
            }
        })
    }

    private fun filterImoveis(filter: String) {
        imoveisList.clear()
        when (filter) {
            "Todos" -> imoveisList.addAll(allImoveisList)
            "Aluguel" -> imoveisList.addAll(allImoveisList.filter { it.modalidade == "Aluguel" })
            "Venda" -> imoveisList.addAll(allImoveisList.filter { it.modalidade == "Venda" })
        }
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(imovel: Imovel) {
        val intent = Intent(requireContext(), ImovelDetailActivity::class.java)
        intent.putExtra("IMOVEL_EXTRA", imovel)
        startActivity(intent)
    }
}
