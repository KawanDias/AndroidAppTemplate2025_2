package com.ifpr.androidapptemplate.ui.imovel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel

class ImovelListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImovelAdapter
    private var imoveisList: MutableList<Imovel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_imovel_list, container, false)

        recyclerView = view.findViewById(R.id.rvImoveis)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ImovelAdapter(imoveisList)
        recyclerView.adapter = adapter

        fetchImoveis()

        return view
    }

    private fun fetchImoveis() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("destaques")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imoveisList.clear()
                for (imovelSnapshot in snapshot.children) {
                    val imovel = imovelSnapshot.getValue(Imovel::class.java)
                    if (imovel != null) {
                        imoveisList.add(imovel)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erro
            }
        })
    }
}
