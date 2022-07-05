package com.ferpa.machinestock.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.MiniCardDetailBinding
import com.ferpa.machinestock.model.*
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

                if (item.getMachinePhotoList().isNotEmpty()) {
                    //Todo make imgScrUrl for thumbnail
                    FirebaseStorage.getInstance().reference.child(item.getMachinePhotoList()[0].imgSrcUrl.toString()).downloadUrl.addOnSuccessListener {
                        Picasso.get()
                            .load(it)
                            .into(photoView)
                    }
                } else {
                    photoView.visibility = View.GONE
                }
            }
        }

        private fun bindTextView(txtView: TextView, itemDetail: String?) {
            if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail == "") || (itemDetail.equals(
                    "NO ESPECÍFICA"
                ))
            ) {
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
