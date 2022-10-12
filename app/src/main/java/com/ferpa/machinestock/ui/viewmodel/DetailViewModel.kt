package com.ferpa.machinestock.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.addNewPhoto
import com.ferpa.machinestock.utilities.PhotoListManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel
@Inject
constructor (private val machinesRepository: MachinesRepository): ViewModel() {

    private val _machine = MutableLiveData<Item>()
    val machine: LiveData<Item> get() = _machine

    lateinit var currentPhotoPath: String

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    private val _shareWithPrice = machinesRepository.getWithPrice()
    val shareWithPrice = _shareWithPrice

    fun getMachine(machineId: Long) {
        viewModelScope.launch {
             _machine.value = machinesRepository.getItem(machineId).first()
        }
    }

    private fun updateStatus(item: Item) {
        viewModelScope.launch {
            machinesRepository.updateStatus(item)
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            machinesRepository.updateItem(item)
        }
    }

    /*
     * Image Business Logic
     */
    fun uploadPhoto(uri: Uri, isThumbnail: Boolean = false) {

        val machinePhotosRef = if (!isThumbnail) {
            storageRef.child(machine.value!!.addNewPhoto())
        } else {
            storageRef.child(machine.value!!.addNewPhoto() + "t")
        }

        val uploadTask = machinePhotosRef.putFile(uri)

        var uriPath: String

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            machinePhotosRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful && !isThumbnail) {
                uriPath = task.result.path.toString()
                uriPath.let { Log.d("Uri Path", it) }
                machine.value?.let {
                    updateItem(PhotoListManager.addNewPhoto(it))
                }
                Log.d(
                    "Firestorage",
                    "Upload Image Succes - name: ${machinePhotosRef.name} -- path:${machinePhotosRef.path}"
                )
            } else {
                Log.d("Firestorage", "Upload Image Error ${task.exception.toString()}")
            }
        }
    }

    fun setUpdateItem(
        item: Item,
        product: String,
        type: String?,
        feature1: String,
        feature2: String,
        feature3: String,
        price: String?,
        brand: String?,
        insideNumber: String?,
        location: String?,
        currency: String?,
        status: String?,
        owner2: String?,
        owner1: String?,
        observations: String?
    ) {
        var capType = ""

        if (type != null) {
            if (type.isNotEmpty()) {
                capType = type[0].toString()
            }
        }

        val editItem =
            getEditItemEntry(
                item,
                product,
                capType,
                feature1,
                feature2,
                feature3,
                stringToDoubleOrEmptyToZero(price),
                brand,
                insideNumber,
                location,
                currency,
                status?.uppercase(),
                getAddOwner(owner2),
                getAddOwner(owner1),
                observations
            )
        updateItem(editItem)
    }

    private fun getEditItemEntry(
        item: Item,
        product: String,
        type: String?,
        feature1: String,
        feature2: String,
        feature3: String,
        price: Double,
        brand: String?,
        insideNumber: String?,
        location: String?,
        currency: String?,
        status: String?,
        owner2: Int?,
        owner1: Int?,
        observations: String?
    ): Item {
        return Item(
            id = item.id,
            insertDate = item.insertDate,
            product = product.uppercase(),
            insideNumber = insideNumber,
            location = location,
            brand = brand,
            feature1 = feature1.toDoubleOrNull(),
            feature2 = feature2.toDoubleOrNull(),
            feature3 = feature3,
            price = price,
            owner1 = owner1,
            owner2 = owner2,
            currency = currency,
            type = type,
            status = status?.uppercase(),
            observations = observations,
            editUser = Firebase.auth.currentUser?.displayName.toString(),
            photos = item.photos
        )
    } //Build edit item Object


    fun setUpdateStatus(status: String?) {
        updateStatus(getNewStatusEntry(machine.value!!.copy(), status))
    }

    private fun getNewStatusEntry(
        item: Item,
        status: String?,
    ): Item {
        return Item(
            id = item.id,
            insertDate = item.insertDate,
            product = item.product,
            insideNumber = item.insideNumber,
            location = item.location,
            brand = item.brand,
            feature1 = item.feature1,
            feature2 = item.feature2,
            feature3 = item.feature3,
            price = item.price,
            owner1 = item.owner1,
            owner2 = item.owner2,
            currency = item.currency,
            type = item.type,
            status = status?.uppercase(),
            observations = item.observations,
            editUser = Firebase.auth.currentUser?.displayName.toString(),
            photos = item.photos
        )
    } //Build new status item Object

    private fun getAddOwner(owner: String?): Int {

        return if (owner != null) {
            if (owner.isNotEmpty()) {
                owner.toInt()
            } else 0
        } else 0

    }

    /*
     * Utils
     */
    private fun stringToDoubleOrEmptyToZero(nullVariable: String?): Double {
        var newDouble = 0.0
        if (nullVariable != "" && nullVariable != null) {
            newDouble = nullVariable.replace(",", "").toDouble()
        }
        return newDouble
    }
}