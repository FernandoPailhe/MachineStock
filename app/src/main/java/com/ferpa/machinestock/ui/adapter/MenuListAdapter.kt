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
    ListAdapter<MainMenuItem, MenuListAdapter.MenuItemViewHolder>(DiffCallBack) {

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

        fun bind(mainMenuItem: MainMenuItem) {

            val miniCardListAdapter = MiniCardListAdapter(object: MiniCardListAdapter.OnChildItemClickListener{
                override fun onChildItemClick(clickPosition: Int) {
                    mainMenuListener.onItemClick(adapterPosition, clickPosition)
                    Log.d("OnItemClick", "override onChildItemClick Main Adapter $adapterPosition / $clickPosition")
                }
            })

            if (mainMenuItem.itemList?.isEmpty() == true || mainMenuItem.itemList == null){
                binding.apply {
                    cardItem.visibility = View.GONE
                    miniCardRecyclerView.visibility = View.GONE
                }
            } else {
                miniCardListAdapter.submitList(mainMenuItem.itemList)
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
        private val DiffCallBack = object : DiffUtil.ItemCallback<MainMenuItem>() {
            override fun areItemsTheSame(oldItem: MainMenuItem, newItem: MainMenuItem): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: MainMenuItem, newItem: MainMenuItem): Boolean {
                return oldItem.name == newItem.name
            }

        }
    }

}
