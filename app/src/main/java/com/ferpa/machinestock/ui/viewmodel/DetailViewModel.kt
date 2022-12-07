package com.ferpa.machinestock.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.businesslogic.DetailUseCases
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
constructor(
    private val detailUseCases: DetailUseCases
) : ViewModel() {

    private val _machine = MutableLiveData<Item>()
    val machine: LiveData<Item> get() = _machine

    lateinit var currentPhotoPath: String

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    fun getMachine(machineId: Long) {
        viewModelScope.launch {
            _machine.value = detailUseCases.getItemUseCase(machineId).first()
        }
    }

    private fun updateStatus(machine: Item) {
        viewModelScope.launch {
            detailUseCases.updateStatusUseCase(machine)
            getMachine(machine.id)
        }
    }

    private fun updateItem(machine: Item) {
        viewModelScope.launch {
            detailUseCases.updateItemUseCase(machine)
            getMachine(machine.id)
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

}