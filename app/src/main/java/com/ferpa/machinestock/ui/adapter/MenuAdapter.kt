package com.ferpa.machinestock.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.data.ProductsSource
import com.ferpa.machinestock.databinding.MenuItemBinding
import com.ferpa.machinestock.model.Product


class MenuAdapter (private val onItemClicked: (Product) -> Unit) : RecyclerView.Adapter<MenuAdapter.ProductViewHolder>(){

    val productList = ProductsSource.products

    class ProductViewHolder(
        private var binding: MenuItemBinding
    ): RecyclerView.ViewHolder(binding.root) {


        fun bind(product: Product) {
            binding.cardTitle.text = product.name.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return MenuAdapter.ProductViewHolder(
            MenuItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val current = productList[position]
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    override fun getItemCount(): Int = productList.size

}
