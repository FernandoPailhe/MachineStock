package com.ferpa.machinestock.ui.adapter

import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ScrollCaptureCallback
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.EditListItemBinding
import com.ferpa.machinestock.model.*

class EditListAdapter(private val onItemClicked: (Item) -> Unit) :
    ListAdapter<Item, EditListAdapter.ItemViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            EditListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        /**
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        **/
        holder.bind(current)

    }

    class ItemViewHolder(
        private var binding: EditListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {



        fun bind(item: Item) {
            binding.apply {
                insideNumberText.text = item.insideNumber
                firstLineText.text = item.getFirstLineInfo()
                secondLineText.text = item.getSecondLineInfo()
                editListPrice.setText(item.getFormattedPrice(false))
                editListPrice.isEnabled = false

                editListPrice.setOnClickListener {
                    saveListAction.isEnabled = true
                    editListPrice.isEnabled = true
                }

                saveListAction.isEnabled = false

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

        const val TAG = "EditListAdapter"
    }

}
