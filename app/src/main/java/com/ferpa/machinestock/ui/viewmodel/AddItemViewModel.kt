package com.ferpa.machinestock.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.businesslogic.AddItemUseCases
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.addNewPhoto
import com.ferpa.machinestock.utilities.Extensions.getAddOwner
import com.ferpa.machinestock.utilities.Extensions.getCapTypeOrEmpty
import com.ferpa.machinestock.utilities.Extensions.stringToDoubleOrEmptyToZero
import com.ferpa.machinestock.utilities.PhotoListManager
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel
@Inject
constructor(private val addItemUseCases: AddItemUseCases) : ViewModel() {

    private val _machine = MutableLiveData<Item>()
    val machine: LiveData<Item> get() = _machine

    lateinit var currentPhotoPath: String

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    fun getMachine(machineId: Long) {
        viewModelScope.launch {
            _machine.value = addItemUseCases.getItemUseCase(machineId).first()
        }
    }

    /*
    Image Business Logic
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

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            addItemUseCases.updateItemUseCase(item)
        }
    }

    fun addNewItem(
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

        val newItem =
            addItemUseCases.getNewItemEntryUseCase(
                product,
                type.getCapTypeOrEmpty(),
                feature1,
                feature2,
                feature3,
                price.stringToDoubleOrEmptyToZero(),
                brand,
                insideNumber,
                location,
                currency,
                status?.uppercase(),
                owner2.getAddOwner(),
                owner1.getAddOwner(),
                observations
            )
        insertItem(newItem)
        getMachine(newItem.id)
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
        updateItem(addItemUseCases.getEditItemEntryUseCase(
            item,
            product,
            type.getCapTypeOrEmpty(),
            feature1,
            feature2,
            feature3,
            price.stringToDoubleOrEmptyToZero(),
            brand,
            insideNumber,
            location,
            currency,
            status?.uppercase(),
            owner2.getAddOwner(),
            owner1.getAddOwner(),
            observations
        ))
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            addItemUseCases.insertItemUseCase(item)
        }
    }

    fun isEntryValid(
        product: String,
        itemFeature1: String,
        itemFeature2: String,
        owner1: String?,
        owner2: String?
    ) = addItemUseCases.isEntryValidUseCase(
        product,
        itemFeature1,
        itemFeature2,
        owner1,
        owner2
    )

}