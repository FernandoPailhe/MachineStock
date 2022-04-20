package com.ferpa.machinestock.ui.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.PhotoItemBinding
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.ui.adapter.PhotoAdapter.MachinePhotoViewHolder
import com.squareup.picasso.Picasso

class PhotoAdapter(
    private val machinePhotoList: List<MachinePhoto>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MachinePhotoViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MachinePhotoViewHolder {
        return MachinePhotoViewHolder(
            PhotoItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: MachinePhotoViewHolder, position: Int) {
        val machinePhotoItem = machinePhotoList[position]
        holder.bind(machinePhotoItem, machinePhotoList)

    }

    inner class MachinePhotoViewHolder(
        private var binding:
        PhotoItemBinding
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

        fun bind(machinePhoto: MachinePhoto, machinePhotoList: List<MachinePhoto>) {

            if (machinePhoto.id == -1) {
                binding.itemPhoto.setImageResource(R.drawable.ic_broken_image)
            } else if(machinePhoto. id == 10) {
                val takenImage = BitmapFactory.decodeFile(machinePhoto.imgSrcUrl)
                binding.itemPhoto.setImageBitmap(takenImage)
                itemView.isEnabled = false //prevent error from new product
            }else {
                machinePhoto.imgSrcUrl?.let {
                    Picasso.get().load(machinePhoto.imgSrcUrl).into(binding.itemPhoto)
                }
                binding.itemId.text =
                    machinePhoto.id.toString()

                if (machinePhoto.imgSrcUrl == null) {
                    binding.itemPhotoCard.alpha = 0f
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount() = machinePhotoList.size


}