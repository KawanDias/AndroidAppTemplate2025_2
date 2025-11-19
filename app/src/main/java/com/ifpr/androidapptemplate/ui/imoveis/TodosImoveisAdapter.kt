package com.ifpr.androidapptemplate.ui.imoveis

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemMeuImovelBinding
import com.ifpr.androidapptemplate.model.Imovel
import java.text.NumberFormat
import java.util.Locale

class TodosImoveisAdapter(
    private val imoveis: List<Imovel>,
    private val onEditClick: (Imovel) -> Unit,
    private val onDeleteClick: (Imovel) -> Unit
) : RecyclerView.Adapter<TodosImoveisAdapter.TodosImoveisViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodosImoveisViewHolder {
        val binding = ItemMeuImovelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodosImoveisViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodosImoveisViewHolder, position: Int) {
        holder.bind(imoveis[position])
    }

    override fun getItemCount(): Int = imoveis.size

    inner class TodosImoveisViewHolder(private val binding: ItemMeuImovelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imovel: Imovel) {
            binding.tvMeuImovelTitulo.text = imovel.titulo

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvMeuImovelPreco.text = formatador.format(imovel.preco)

            imovel.base64Images.firstOrNull()?.let {
                try {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imgMeuImovel.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.imgMeuImovel.setImageResource(R.drawable.placeholder_image)
                }
            } ?: binding.imgMeuImovel.setImageResource(R.drawable.placeholder_image)

            binding.btnEditarImovel.visibility = View.VISIBLE
            binding.btnExcluirImovel.visibility = View.VISIBLE

            binding.btnEditarImovel.setOnClickListener { onEditClick(imovel) }
            binding.btnExcluirImovel.setOnClickListener { onDeleteClick(imovel) }
        }
    }
}
