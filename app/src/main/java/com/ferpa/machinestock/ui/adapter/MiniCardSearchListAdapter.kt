package com.ferpa.machinestock.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.MiniCardDetailBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.utilities.Extensions.loadImage
import com.ferpa.machinestock.utilities.PhotoListManager

class MiniCardSearchListAdapter(
    private val onItemClicked: (Item) -> Unit,
    private val isAllProductsList: Boolean = false
) :
    ListAdapter<Item, MiniCardSearchListAdapter.ItemViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            MiniCardDetailBinding.inflate(
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
        holder.bind(current, isAllProductsList)

    }

    class ItemViewHolder(
        private var binding: MiniCardDetailBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(machine: Item, isAllProductsList: Boolean) {
            binding.apply {
                miniCardDetail.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                miniCardImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                val cardTitle = if (isAllProductsList) "${machine.product} - ${machine.getBrand()}" else machine.getBrand()
                bindTextView(itemBrand, cardTitle)
                bindTextView(itemFeature1, machine.getFeatures())
                bindTextView(itemPrice, machine.getFormattedPrice(true))
                bindTextView(itemStatus, machine.getStatus())
                miniCardImageView.loadImage(machine.product, PhotoListManager.getMachinePhotoList(machine, true)[0], true)
                cardBrand.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        private fun bindTextView(txtView: TextView, itemDetail: String?) {
            if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail == " -  ") || (itemDetail == "NO ESPECÍFICA")) {
                txtView.visibility = View.GONE
            } else {
                txtView.text = itemDetail
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
