package com.ferpa.machinestock.utilities

import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ferpa.machinestock.R
import com.ferpa.machinestock.model.MachinePhoto
import com.google.firebase.storage.FirebaseStorage

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
                    Glide.with(this.context)
                        .load(uri)
                        .placeholder(photoResource)
                        .fitCenter()
                        .centerCrop()
                        .into(this)
                } else {
                    Glide.with(this.context)
                        .load(uri)
                        .transform(RoundedCorners(16))
                        .placeholder(photoResource)
                        .into(this)
                }
            }.addOnFailureListener {
                Log.e("ImageViewLoadImageExtensionFunction", it.message.toString())
            }
        }
    }

    fun String?.stringToDoubleOrEmptyToZero(): Double {
        var newDouble = 0.0
        if (this != "" && this != null) {
            newDouble = this.replace(",", "").toDouble()
        }
        return newDouble
    }

    fun String?.stringToIntOrEmptyToZero(): Int {
        var newInt = 0
        if (this != "" && this != null) {
            newInt = this.toInt()
        }
        return newInt
    }

    fun String?.getCapTypeOrEmpty() = if (this.isNullOrEmpty()) "" else this[0].toString()

    fun String?.getAddOwner() = if (this.isNullOrEmpty()) 0 else this.toInt()

}