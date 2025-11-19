package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel

class MeusFavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritosAdapter
    private val favoritosList = mutableListOf<Imovel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meus_favoritos, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_favoritos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FavoritosAdapter(favoritosList)
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFavoritos()
    }

    private fun loadFavoritos() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/favoritos")
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoritosList.clear()
                    for (postSnapshot in snapshot.children) {
                        val imovel = postSnapshot.getValue(Imovel::class.java)
                        val imovelKey = postSnapshot.key
                        if (imovel != null && imovelKey != null) {
                            imovel.key = imovelKey
                            favoritosList.add(imovel)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}