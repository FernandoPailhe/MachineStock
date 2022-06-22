package com.ferpa.machinestock.ui.adapter

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.databinding.PhotoItemBinding
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.ui.adapter.PhotoAdapter.MachinePhotoViewHolder
import com.ferpa.machinestock.utilities.ExifInfo
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.IOException

const val TAG = "PhotoAdapter"

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
        holder.bind(machinePhotoItem)

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

        fun bind(machinePhoto: MachinePhoto) {

            if (machinePhoto.imgSrcUrl == null || machinePhotoList.isEmpty() || machinePhoto.id == -1) {
                binding.itemPhotoCard.apply {
                    alpha = 0f
                    isGone
                }
            } else {
                FirebaseStorage.getInstance().reference.child(machinePhoto.imgSrcUrl.toString()).downloadUrl.addOnSuccessListener {
                    Picasso.get()
                        .load(it)
                        //.rotate(ExifInfo.getOrientation(binding.itemPhoto.context, it).toFloat())
                        .into(binding.itemPhoto)
                        /**
                        .into(binding.itemPhoto, object: com.squareup.picasso.Callback{
                            override fun onSuccess() {
                                TODO("Loading Animation")
                            }
                            override fun onError(e: Exception?) {
                                TODO("Not yet implemented")
                            }
                        })
                        **/
                    Log.d(
                        TAG,
                        "Succes to retrieve image from ${machinePhoto.imgSrcUrl} with Picasso"
                    )
                }.addOnFailureListener {
                    Log.d(TAG, "Failed to retrieve image from ${machinePhoto.imgSrcUrl} with Picasso")
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount() = machinePhotoList.size


}