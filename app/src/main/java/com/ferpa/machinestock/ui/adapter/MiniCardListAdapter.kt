package com.ferpa.machinestock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.MiniCardDetailBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.utilities.PhotoListManager
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

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

        fun bind(item: Item) {
            binding.apply {
                //Todo design de product icon
                bindTextView(itemProduct, item.product[0].toString())
                itemProduct.visibility = View.GONE

                bindTextView(itemBrand, item.brand)
                bindTextView(itemFeature1, item.getFeatures())
                bindTextView(itemFeature3, item.feature3)
                bindTextView(itemType, " - ${item.getType()[0]}")
                bindTextView(itemPrice, item.getFormattedPrice(true))
                bindTextView(itemStatus, item.getStatus())
                bindPhoto(item)

            }
        }

        private fun bindTextView(txtView: TextView, itemDetail: String?) {
            if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail == "") || (itemDetail == "NO ESPECÍFICA")
            ) {
                txtView.visibility = View.GONE
            } else {
                txtView.text = itemDetail
            }
        }

        private fun bindPhoto(item: Item) {
            if (item.getMachinePhotoList()[0].id == -1) {
                val photoResource = when (item.product) {
                    "GUILLOTINA" -> R.drawable.s_guillotina
                    "PLEGADORA" -> R.drawable.s_plegadora
                    "BALANCIN" -> R.drawable.s_balancin
                    "TORNO" -> R.drawable.s_torno
                    "FRESADORA" -> R.drawable.s_fresadora
                    "PLASMA" -> R.drawable.s_plasma
                    else -> R.drawable.ic_machine_icon
                }
                binding.miniCardImageView.setImageResource(photoResource)
            } else {
                FirebaseStorage.getInstance().reference.child(PhotoListManager.getMachinePhotoList(item,true)[0].imgSrcUrl.toString()).downloadUrl.addOnSuccessListener {
                    Picasso.get()
                        .load(it)
                        .into(binding.miniCardImageView)
                }
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
