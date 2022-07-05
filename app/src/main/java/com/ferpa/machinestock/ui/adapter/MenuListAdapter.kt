package com.ferpa.machinestock.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.MenuCardLayoutBinding
import com.ferpa.machinestock.model.*

class MenuListAdapter(private val mainMenuListener: OnItemClickListener) :
    ListAdapter<MenuItem, MenuListAdapter.MenuItemViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MenuItemViewHolder {
        return MenuItemViewHolder(
            MenuCardLayoutBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)

    }

    inner class MenuItemViewHolder(
        private var binding: MenuCardLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(mainMenuItem: MenuItem) {

            val miniCardListAdapter = MiniCardListAdapter(object: MiniCardListAdapter.OnChildItemClickListener{

                override fun onChildItemClick(clickPosition: Int) {
                    mainMenuListener.onItemClick(adapterPosition, clickPosition)
                    Log.d("OnItemClick", "override onChildItemClick Main Adapter $adapterPosition / $clickPosition")
                }
            })

            miniCardListAdapter.submitList(mainMenuItem.itemList)

            Log.d("MenuListAdapterMainMenu","${mainMenuItem.itemList?.size}" )

            binding.apply {
                cardMenuTitle.text = mainMenuItem.name
                miniCardRecyclerView.layoutManager = LinearLayoutManager(
                    miniCardRecyclerView.context,
                    RecyclerView.HORIZONTAL,
                    false
                )
                miniCardRecyclerView.adapter = miniCardListAdapter
            }

        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                mainMenuListener.onItemClick(adapterPosition, -1)
            }
            Log.d("OnItemClick", "override onClick Main Adapter $adapterPosition / No Item")

        }

    }

    interface OnItemClickListener {
        fun onItemClick(mainPosition: Int, childPosition: Int)
    }

    companion object {
        private val DiffCallBack = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem.name == newItem.name
            }

        }
    }

}
