package com.ifpr.androidapptemplate.ui.dashboard

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Referência do banco de dados para a coleção de todos os imóveis
    private val databaseRef = FirebaseDatabase.getInstance().getReference("imoveis")

    // Listener de valor para receber atualizações em tempo real
    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Verifica se a View ainda existe
            val container = view?.findViewById<LinearLayout>(R.id.itemContainerDashboard)
            if (container == null) {
                Log.w("DashboardFragment", "Container de itens não encontrado.")
                return
            }

            container.removeAllViews() // Limpa antes de reconstruir

            var count = 0
            for (itemSnapshot in snapshot.children) {
                val item = itemSnapshot.getValue(Item::class.java)

                if (item == null) {
                    Log.w("DashboardFragment", "Item nulo encontrado, pulando.")
                    continue
                }

                // Garante que a chave do Firebase esteja atribuída ao objeto Item
                item.key = itemSnapshot.key
                count++

                // Infla o template de item
                val itemView = LayoutInflater.from(container.context)
                    .inflate(R.layout.item_template, container, false)

                val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                val precoView = itemView.findViewById<TextView>(R.id.item_preco)

                // Exibe detalhes (usando titulo como endereço se o endereço for nulo)
                enderecoView.text = item.titulo ?: item.endereco ?: "Sem Título/Endereço"
                precoView.text = "R$ ${String.format("%.2f", item.preco)}"

                // Adiciona um listener de clique (exemplo)
                itemView.setOnClickListener {
                    Toast.makeText(context, "Detalhes do Imóvel: ${item.titulo}", Toast.LENGTH_SHORT).show()
                }

                // Exibe imagem Base64
                if (!item.base64Image.isNullOrEmpty()) {
                    try {
                        val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("DashboardFragment", "Erro ao decodificar Base64: ${e.message}")
                        // Substitua por um ID de recurso existente no seu projeto
                        imageView.setImageResource(R.drawable.placeholder_image)
                    }
                } else {
                    // Substitua por um ID de recurso existente no seu projeto
                    imageView.setImageResource(R.drawable.placeholder_image)
                }

                container.addView(itemView)
            }

            if (count == 0) {
                val vazio = TextView(container.context)
                vazio.text = "Nenhum imóvel cadastrado no catálogo."
                vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                vazio.setPadding(0, 32, 0, 32)
                container.addView(vazio)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(context, "Erro ao carregar catálogo: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adiciona o listener para iniciar a busca por todos os imóveis
        databaseRef.addValueEventListener(valueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Importante: Remove o listener para evitar vazamento de memória
        databaseRef.removeEventListener(valueEventListener)
        _binding = null
    }
}