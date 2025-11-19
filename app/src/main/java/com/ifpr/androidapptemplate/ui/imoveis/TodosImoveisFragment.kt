package com.ifpr.androidapptemplate.ui.imoveis

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.databinding.FragmentTodosImoveisBinding
import com.ifpr.androidapptemplate.model.Imovel
import com.ifpr.androidapptemplate.ui.server.ImovelManagementActivity

class TodosImoveisFragment : Fragment() {

    private var _binding: FragmentTodosImoveisBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var imoveisReference: DatabaseReference
    private lateinit var imoveisAdapter: TodosImoveisAdapter
    private val imoveisList = mutableListOf<Imovel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosImoveisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        imoveisReference = FirebaseDatabase.getInstance().getReference("imoveis")
        val currentUser = auth.currentUser

        setupRecyclerView()

        if (currentUser != null) {
            fetchMeusImoveis(currentUser.uid)
        } else {
            if (isAdded) {
                Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        imoveisAdapter = TodosImoveisAdapter(
            imoveisList,
            onEditClick = { imovel ->
                val intent = Intent(requireContext(), ImovelManagementActivity::class.java)
                intent.putExtra("IMOVEL_ID", imovel.key)
                startActivity(intent)
            },
            onDeleteClick = { imovel ->
                showDeleteConfirmationDialog(imovel)
            }
        )
        binding.rvTodosImoveis.layoutManager = LinearLayoutManager(context)
        binding.rvTodosImoveis.adapter = imoveisAdapter
    }

    private fun fetchMeusImoveis(userId: String) {
        imoveisReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imoveisList.clear()
                for (imovelSnapshot in snapshot.children) {
                    val imovel = imovelSnapshot.getValue(Imovel::class.java)
                    if (imovel != null) {
                        imoveisList.add(imovel)
                    }
                }
                imoveisAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Erro ao carregar imóveis.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showDeleteConfirmationDialog(imovel: Imovel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Imóvel")
            .setMessage("Tem certeza de que deseja excluir este imóvel? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                deleteImovel(imovel)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteImovel(imovel: Imovel) {
        val userId = auth.currentUser?.uid ?: return
        val imovelKey = imovel.key

        val imovelRef = imoveisReference.child(userId).child(imovelKey)
        val destaqueRef = FirebaseDatabase.getInstance().getReference("destaques").child(imovelKey)

        imovelRef.removeValue().addOnSuccessListener {
            destaqueRef.removeValue().addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(context, "Imóvel excluído com sucesso.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(context, "Falha ao excluir o destaque.", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                Toast.makeText(context, "Falha ao excluir o imóvel.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
