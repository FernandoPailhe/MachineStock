package com.ferpa.machinestock.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import javax.inject.Inject


@HiltViewModel
class ItemListViewModel
@Inject
constructor(private val machinesRepository: MachinesRepository) : ViewModel() {

    private val _filterList = MutableLiveData<List<Item>>()
    val filterList: LiveData<List<Item>> get() = _filterList

    private val _isNewFilter = MutableStateFlow(true)
    val isNewFilter: StateFlow<Boolean> get() = _isNewFilter

    private val _itemListPosition = MutableStateFlow (0)
    val itemListPosition: MutableStateFlow<Int> get () = _itemListPosition

    fun getProduct(): String = machinesRepository.customListUtil.getProduct()

    val productArray: LiveData<List<String>> = machinesRepository.productArray.asLiveData()

    init {
        collectFilterList()
    }

    fun collectFilterList() {
        viewModelScope.launch {
            machinesRepository.itemsFlow.collect(){ collectedFilterList ->
                _filterList.postValue(collectedFilterList)
            }
        }
    }

    /*
    Update Custom List Util
     */
    fun setProduct(newProduct: String) {
        machinesRepository.setProduct(newProduct)
        Log.d("ItemListViewModel", "New Filter Product -> $newProduct")
        _isNewFilter.value = true
    }

    fun setNewFilterFalse() {
        _isNewFilter.value = false
    }

    fun setQueryText(inputSearch: String?) {
        machinesRepository.setQueryText(inputSearch)
        _isNewFilter.value = true
    }

    fun getFilterStatus(type: String): Boolean {
        return machinesRepository.getFilterStatus(type)
    }

    fun setFilter(type: String) {
        machinesRepository.setFilter(type)
        _isNewFilter.value = true
    }

    fun isFilterList(): Boolean {
        return machinesRepository.isFilterList()
    }

    fun clearFilters() {
        machinesRepository.clearFilters()
        _isNewFilter.value = true
    }
}