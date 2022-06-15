package com.ferpa.machinestock.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.FullScreenPhotoItemBinding
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.ui.adapter.FullScreenPhotoAdapter.MachineFullScreenPhotoViewHolder
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class FullScreenPhotoAdapter(
    private val machinePhotoList: List<MachinePhoto>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MachineFullScreenPhotoViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MachineFullScreenPhotoViewHolder {
        return MachineFullScreenPhotoViewHolder(
            FullScreenPhotoItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: MachineFullScreenPhotoViewHolder, position: Int) {
        val machinePhotoItem = machinePhotoList[position]
        holder.bind(machinePhotoItem)

    }

    inner class MachineFullScreenPhotoViewHolder(
        private var binding:
        FullScreenPhotoItemBinding
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

            FirebaseStorage.getInstance().reference.child(machinePhoto.imgSrcUrl.toString()).downloadUrl.addOnSuccessListener {
                Picasso.get()
                    .load(it)
                    .networkPolicy(NetworkPolicy.OFFLINE)//For cache the images
                    .rotate(90.0F)
                    .into(binding.fullScreenPhoto)
                Log.d(TAG, "Succes to retrieve image from ${machinePhoto.imgSrcUrl} with Picasso")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to retrieve image with Picasso")
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount() = machinePhotoList.size


}