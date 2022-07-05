package com.ferpa.machinestock.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.ListItemBinding
import com.ferpa.machinestock.model.*


class AllItemsListAdapter(private val onItemClicked: (Item) -> Unit) :
    ListAdapter<Item, AllItemsListAdapter.ItemViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)

    }

    class ItemViewHolder(
        private var binding: ListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.apply {
                itemUpText.text = item.getName()
                itemDownText.text = item.getFeatures()
                priceText.text = item.getFormattedPrice(true)
                statusText.text = item.status
                itemLayout.setBackgroundResource(item.getBackgroundColor())
                itemProductText.text = item.product
            }
        }
    }


    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}