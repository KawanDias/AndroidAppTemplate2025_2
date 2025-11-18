package com.ifpr.androidapptemplate.ui.imovel

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.androidapptemplate.R

class ImageSliderAdapter(
    private val images: List<String>,
    private val isBase64: Boolean
) : RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    class ImageSliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view_slider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageSliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val image = images[position]
        if (isBase64) {
            try {
                val bytes = Base64.decode(image, Base64.DEFAULT)
                holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (e: Exception) {
                holder.imageView.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            Glide.with(holder.itemView.context).load(image).into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = images.size
}