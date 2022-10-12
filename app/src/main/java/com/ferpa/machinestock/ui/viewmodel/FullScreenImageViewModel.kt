package com.ferpa.machinestock.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.utilities.PhotoListManager
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullScreenImageViewModel
@Inject
constructor(private val machinesRepository: MachinesRepository) : ViewModel() {

    private val _machine = MutableLiveData<Item>()
    val machine: LiveData<Item> get() = _machine

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    fun getMachine(machineId: Long) {
        viewModelScope.launch {
            _machine.value = machinesRepository.getItem(machineId).first()
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            machinesRepository.updateItem(item)
        }
    }

    fun deletePhoto(item: Item, deletePhoto: Int) {

        val photoRef =
            storageRef.child(PhotoListManager.getDeleteUrl(item, deletePhoto))

        photoRef.delete()
            .addOnSuccessListener {
                Log.d("FullScreenImageViewModel", "onSuccess: deleted file $deletePhoto")
                updateItem(PhotoListManager.deletePhoto(item, deletePhoto))
            }.addOnFailureListener {
                Log.d("FullScreenImageViewModel", "onFailure: did not delete file $deletePhoto")
            }

        val thumbRef = storageRef.child(
            PhotoListManager.getDeleteUrl(
                item,
                deletePhoto
            ) + "t"
        )

        thumbRef.delete()
            .addOnSuccessListener {
                Log.d("FullScreenImageViewModel", "onSuccess: deleted file $deletePhoto t")
            }.addOnFailureListener {
                Log.d("FullScreenImageViewModel", "onFailure: did not delete file $deletePhoto t")
            }

    }

}