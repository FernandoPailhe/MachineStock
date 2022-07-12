package com.ferpa.machinestock.ui.viewmodel


import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.data.ItemRepository
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.utilities.Const
import com.ferpa.machinestock.utilities.PhotoListManager
import com.ferpa.machinestock.utilities.PhotoListManager.Companion.addNewPhoto
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


const val TAG = "MachineStockViewModel"

@HiltViewModel
class MachineStockViewModel
@Inject
constructor(private val itemRepository: ItemRepository) :
    ViewModel() {

    lateinit var currentPhotoPath: String

    val filterItems: Flow<List<Item>> = itemRepository.itemsFlow

    val mainMenuItemListFlow: Flow<List<MainMenuItem>> = itemRepository.menuList

    private val _isNewFilter = MutableStateFlow(true)
    val isNewFilter: StateFlow<Boolean> get() = _isNewFilter

    private val _currentId = MutableStateFlow<Long>(1)
    val currentId: StateFlow<Long> get() = _currentId

    private var _currentItem: LiveData<Item> = itemRepository.getItem(currentId.value).asLiveData()
    val currentItem: LiveData<Item> get() = _currentItem

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var storageRef = storage.reference

    init {
        compareDatabases()

        /* //TODO Delete when this will not more necessary
        viewModelScope.launch {
        itemRepository.populateDb()
        }
         */

    }

    fun getProduct(): String = itemRepository.customListUtil.getProduct()

    fun setCurrentId(id: Long) {
        _currentId.value = id
        _currentItem = itemRepository.getItem(currentId.value).asLiveData()
    }

    /*
    Network
     */
    private fun compareDatabases() {
        viewModelScope.launch {
            try {
                itemRepository.compareDatabases()
            } catch (e: Exception) {
                Log.d("CompareNetworkItems", e.toString())

            }
        }
    }

    /*
    Image Business Logic
     */
    fun uploadPhoto(uri: Uri, isThumbnail: Boolean = false) {

        //TODO Progress animation
        val machinePhotosRef = if (!isThumbnail) {
            storageRef.child(currentItem.value!!.addNewPhoto())
        } else {
            storageRef.child(currentItem.value!!.addNewPhoto() + "t")
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
                currentItem.value?.let {
                    //updateItem(it.updatePhotos(currentItem.value!!.addNewPhoto().split("_").last()))
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

    fun deletePhoto(item: Item, deletePhoto: Int) {

        val photoRef =
            storageRef.child(PhotoListManager.getDeleteUrl(item, deletePhoto))

        photoRef.delete()
            .addOnSuccessListener(OnSuccessListener<Void?> {
                Log.d(TAG, "onSuccess: deleted file $deletePhoto")
                updateItem(PhotoListManager.deletePhoto(item, deletePhoto))
            }).addOnFailureListener(OnFailureListener {
                Log.d(TAG, "onFailure: did not delete file $deletePhoto")
            })

        val thumbRef = storageRef.child(
            PhotoListManager.getDeleteUrl(
                item,
                deletePhoto
            ) + "t"
        )

        thumbRef.delete()
            .addOnSuccessListener(OnSuccessListener<Void?> {
                Log.d(TAG, "onSuccess: deleted file $deletePhoto t")
            }).addOnFailureListener(OnFailureListener {
                Log.d(TAG, "onFailure: did not delete file $deletePhoto t")
            })

    }

    /*
    Update Custom List Util
     */
    fun setProduct(newProduct: String) {
        itemRepository.customListUtil.setProduct(newProduct)
        _isNewFilter.value = true
    }

    fun setNewFilterFalse() {
        _isNewFilter.value = false
    }

    fun setQueryText(inputSearch: String?) {
        itemRepository.customListUtil.setQueryText(inputSearch)
        _isNewFilter.value = true
    }

    fun getFilterStatus(type: String): Boolean {
        return itemRepository.customListUtil.getFilterStatus(type)
    }

    fun setFilter(type: String) {
        itemRepository.customListUtil.setFilter(type)
        _isNewFilter.value = true
    }

    fun isFilterList(): Boolean {
        return itemRepository.customListUtil.isFilteredList
    }

    fun clearFilters() {
        itemRepository.customListUtil.clearFilters()
        _isNewFilter.value = true
    }

    //Manage Single Item
    fun retrieveItem(id: Long): LiveData<Item> {
        return itemRepository.getItem(id).asLiveData()
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
            itemRepository.postItem(item)
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemRepository.updateItem(item)
        }
    }

    private fun getNewItemEntry(
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
            product = product.uppercase(),
            insideNumber = insideNumber,
            location = location,
            brand = brand,
            feature1 = feature1.toDouble(),
            feature2 = feature2.toDoubleOrNull(),
            feature3 = feature3,
            price = price,
            owner1 = owner1,
            owner2 = owner2,
            currency = currency,
            type = type,
            status = status,
            observations = observations,
            editUser = "",
        )
    } //Build new item Object

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
            feature1 = feature1.toDouble(),
            feature2 = feature2.toDouble(),
            feature3 = feature3,
            price = price,
            owner1 = owner1,
            owner2 = owner2,
            currency = currency,
            type = type,
            status = status,
            observations = observations,
            photos = item.photos
        )
    } //Build edit item Object

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
        var capType = ""

        if (type != null) {
            if (type.isNotEmpty()) {
                capType = type[0].toString()
            }
        }

        val newItem =
            getNewItemEntry(
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
                status,
                getAddOwner(owner2),
                getAddOwner(owner1),
                observations
            )
        insertItem(newItem)
        setCurrentId(newItem.id)
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
                status,
                getAddOwner(owner2),
                getAddOwner(owner1),
                observations
            )
        updateItem(editItem)
    }

    private fun getAddOwner(owner: String?): Int {

        return if (owner != null) {
            if (owner.isNotEmpty()) {
                owner.toInt()
            } else 0
        } else 0

    }

    fun isEntryValid(
        product: String,
        itemFeature1: String,
        itemFeature2: String,
        owner1: String?,
        owner2: String?,
        insideNumber: String?
    ): Int {

        var isValid = 0

        if (!isFeatureEntryValid(product, itemFeature1, itemFeature2)) {
            isValid = 1
        } else if (!isOwnerEntryValid(
                stringToIntOrEmptyToZero(owner1),
                stringToIntOrEmptyToZero(owner2)
            )
        ) {
            isValid = 2
        }
        //TODO isInsideNumberValid?

        return isValid
    }


    //Validates Entries
    private fun isOwnerEntryValid(owner1: Int?, owner2: Int?): Boolean {
        var total = 0
        total = owner1!! + owner2!!

        return (total <= 100)
    }

    private fun isFeatureEntryValid(
        product: String,
        itemFeature1: String,
        itemFeature2: String
    ): Boolean {
        return when (product) {
            "GUILLOTINA" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "PLEGADORA" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "BALANCIN" -> itemFeature1.isNotBlank()
            "TORNO" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "COMPRESOR" -> itemFeature1.isNotBlank()
            "LIMADORA" -> itemFeature1.isNotBlank()
            "SERRUCHO" -> itemFeature1.isNotBlank()
            "SOLDADURA" -> itemFeature1.isNotBlank()
            else -> false
            //TODO make isFeatureEntry for all products
        }
    }

    private fun stringToIntOrEmptyToZero(nullVariable: String?): Int {
        var newInt = 0
        if (nullVariable != "" && nullVariable != null) {
            newInt = nullVariable.toInt()
        }
        return newInt
    }

    private fun stringToDoubleOrEmptyToZero(nullVariable: String?): Double {
        var newDouble = 0.0
        if (nullVariable != "" && nullVariable != null) {
            newDouble = nullVariable.replace(",", "").toDouble()
        }
        return newDouble
    }

}