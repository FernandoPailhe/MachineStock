package com.ferpa.machinestock.ui.adapter

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

class MiniCardListAdapter(
    private val childItemClickListener: OnChildItemClickListener
) :
    ListAdapter<Item, MiniCardListAdapter.ItemViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            MiniCardDetailBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            ), childItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)

    }

    class ItemViewHolder(
        private var binding: MiniCardDetailBinding,
        private val childItemClickListener: OnChildItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                childItemClickListener.onChildItemClick(position)
            }
        }

        fun bind(machine: Item) {
            binding.apply {
                bindTextView(itemProduct, machine.product)
                itemProduct.visibility = View.GONE
                bindTextView(itemBrand, machine.getBrand())
                bindTextView(itemFeature1, machine.getFeatures())
                bindTextView(itemFeature3, machine.feature3)
                bindTextView(itemType, " - ${machine.getType()[0]}")
                bindTextView(itemPrice, machine.getFormattedPrice(true))
                bindTextView(itemStatus, machine.getStatus())
                miniCardImageView.loadImage(machine.product, PhotoListManager.getMachinePhotoList(machine, true)[0], true)
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

    interface OnChildItemClickListener {
        fun onChildItemClick(clickPosition: Int)
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
