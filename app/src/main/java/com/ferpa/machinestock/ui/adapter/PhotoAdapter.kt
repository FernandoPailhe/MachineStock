package com.ferpa.machinestock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.PhotoItemBinding
import com.ferpa.machinestock.databinding.PhotoViewPagerBinding
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.ui.adapter.PhotoAdapter.MachinePhotoViewHolder
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class PhotoAdapter(
    private val machinePhotoList: List<MachinePhoto>,
    private val listener: OnItemClickListener,
    private val photoSize: String = ""
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

            if (machinePhoto.imgSrcUrl == null || machinePhotoList.isEmpty() || machinePhoto.id == -1) {
                binding.apply {
                    when (machinePhoto.imgSrcUrl) {
                        "GUILLOTINA" -> photoImageView.setImageResource(R.drawable.s_guillotina)
                        "PLEGADORA" -> photoImageView.setImageResource(R.drawable.s_plegadora)
                        "BALANCIN" -> photoImageView.setImageResource(R.drawable.s_balancin)
                        "TORNO" -> photoImageView.setImageResource(R.drawable.s_torno)
                        "FRESADORA" -> photoImageView.setImageResource(R.drawable.s_fresadora)
                        "PLASMA" -> photoImageView.setImageResource(R.drawable.s_plasma)
                        else -> photoImageView.setImageResource(R.drawable.ic_machine_icon)
                    }
                }
            } else {
                val photoUrl = machinePhoto.imgSrcUrl.toString() + photoSize

                FirebaseStorage.getInstance().reference.child(photoUrl).downloadUrl.addOnSuccessListener {
                    Picasso.get()
                        .load(it)
                        //.rotate(ExifInfo.getOrientation(binding.itemPhoto.context, it).toFloat())
                        .into(binding.photoImageView)
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
                }.addOnFailureListener {
                    //TODO Manage Exception
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount() = machinePhotoList.size


}