package com.ferpa.machinestock.ui.viewmodel


import androidx.lifecycle.*
import com.ferpa.machinestock.data.ItemRepository
import com.ferpa.machinestock.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MachineStockViewModel
@Inject
constructor(private val itemRepository: ItemRepository) :
    ViewModel() {

    val filterItems: Flow<List<Item>> = itemRepository.itemsFlow

    private var _filterItemsFlow: LiveData<List<Item>> = itemRepository.getCustomQuery().asLiveData()
    val filterItemsFlow: LiveData<List<Item>> get() = _filterItemsFlow


    private val _isNewFilter = MutableStateFlow(true)
    val isNewFilter: StateFlow<Boolean> get() = _isNewFilter

    //Update Custom List Util
    fun setProduct(newProduct: String) {
        itemRepository.customListUtil.setProduct(newProduct)
        _isNewFilter.value = true
    }

    fun setNewFilterFalse(){
        _isNewFilter.value = false
    }

    fun setQueryText(inputSearch: String?) {
        itemRepository.customListUtil.setQueryText(inputSearch)
        _isNewFilter.value = true
    }

    fun getFilterStatus(type: String): Boolean {
        return itemRepository.customListUtil.getFilterStatus(type)
    }

    fun setFilter(type: String){
        itemRepository.customListUtil.setFilter(type)
        _isNewFilter.value = true
    }

    fun isFilterList():Boolean{
        return itemRepository.customListUtil.isFilteredList
    }

    //Manage Single Item
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemRepository.getItem(id).asLiveData()
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemRepository.insertItem(item)
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
        owner1: Int?
    ): Item {
        return Item(
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
            observations = "",
            editDate = "",
            editUser = "",
            excelText = "",
            photo1 = "",
            photo2 = "",
            photo3 = "",
            photo4 = ""
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
        owner1: Int?
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
            photo1 = item.photo1,
            photo2 = item.photo2,
            photo3 = item.photo3,
            photo4 = item.photo4,
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
        owner1: String?
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
                getAddOwner(owner1)
            )
        insertItem(newItem)
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
        owner1: String?
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
                getAddOwner(owner1)
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

    private fun isOwnerEntryValid(owner1: Int?, owner2: Int?): Boolean {
        var total = 0
        total = owner1!! + owner2!!

        return (total <= 100)
    }

    private fun isFeatureEntryValid(product: String, itemFeature1: String, itemFeature2: String): Boolean {
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

    fun clearFilters() {
        itemRepository.customListUtil.clearFilters()
        _isNewFilter.value = true
    }

}