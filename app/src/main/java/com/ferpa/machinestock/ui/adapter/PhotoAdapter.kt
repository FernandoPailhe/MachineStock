package com.ferpa.machinestock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.PhotoViewPagerBinding
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.ui.adapter.PhotoAdapter.MachinePhotoViewHolder
import com.ferpa.machinestock.utilities.Extensions.loadImage

class PhotoAdapter(
    private val product: String,
    private val machinePhotoList: List<MachinePhoto>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MachinePhotoViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MachinePhotoViewHolder {
        return MachinePhotoViewHolder(
            PhotoViewPagerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MachinePhotoViewHolder, position: Int) {
        val machinePhotoItem = machinePhotoList[position]
        holder.bind(machinePhotoItem)

    }

    inner class MachinePhotoViewHolder(
        private var binding:
        PhotoViewPagerBinding
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

        fun bind(machinePhoto: MachinePhoto) {
            binding.photoImageView.loadImage(product, machinePhoto)
        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount() = machinePhotoList.size

}