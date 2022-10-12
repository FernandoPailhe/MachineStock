package com.ferpa.machinestock.utilities

import android.util.Log
import android.widget.ImageView
import com.ferpa.machinestock.R
import com.ferpa.machinestock.model.MachinePhoto
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

object Extensions {

    fun ImageView.loadImage(product: String, machinePhoto: MachinePhoto, fit: Boolean = false) {

        val photoResource = when (product) {
            "GUILLOTINA" -> R.drawable.s_guillotina
            "PLEGADORA" -> R.drawable.s_plegadora
            "BALANCIN" -> R.drawable.s_balancin
            "TORNO" -> R.drawable.s_torno
            "FRESADORA" -> R.drawable.s_fresadora
            "PLASMA" -> R.drawable.s_plasma
            "PLATO" -> R.drawable.s_plato
            "RECTIFICADORA" -> R.drawable.s_rectificadora
            else -> R.drawable.s_torno
        }
        if (machinePhoto.id == -1) {
            this.setImageResource(photoResource)
        } else {
            FirebaseStorage.getInstance().reference.child(machinePhoto.imgSrcUrl.toString()).downloadUrl.addOnSuccessListener { uri ->
                if (fit) {
                    Picasso.get()
                        .load(uri)
                        .placeholder(photoResource)
                        .fit()
                        .centerCrop()
                        .into(this)
                } else {
                    Picasso.get()
                        .load(uri)
                        .placeholder(photoResource)
                        .into(this)
                }
            }.addOnFailureListener {
                Log.e("ImageViewLoadImageExtensionFunction", it.message.toString())
            }
        }
    }

}