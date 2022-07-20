package com.ferpa.machinestock.utilities

import android.util.Log
import com.ferpa.machinestock.data.ItemRepository
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.utilities.Const.OWNER_1
import com.ferpa.machinestock.utilities.Const.OWNER_2
import com.ferpa.machinestock.utilities.Const.SOCIEDAD
import java.util.*

//TODO Clean innecesaries functions
class CustomListUtil {

    private var product: String = "TODAS"

    var isFilteredList: Boolean = false

    private var searchInput: String = "%%"

    private var filterOwner1: Boolean = false

    private var filterOwner2: Boolean = false

    private var filterShared: Boolean = false

    val filterStatusList: MutableList<String> = mutableListOf()

    val filterTypeList: MutableList<String> = mutableListOf()

    fun filterItem(item: Item): Boolean {

        var isAdded = false

        if (filterOwner1 || filterOwner2 || filterShared) {
            isAdded = filterOwner(item)
            if (filterStatusList.isNotEmpty() && isAdded) {
                isAdded = filterStatus(item)
                if (filterTypeList.isNotEmpty() && isAdded) {
                    isAdded = filterType(item)
                }
            }
        } else if (filterStatusList.isNotEmpty()) {
            isAdded = filterStatus(item)
            if (filterTypeList.isNotEmpty() && isAdded) {
                isAdded = filterType(item)
            }
        } else if (filterTypeList.isNotEmpty()) {
            isAdded = filterType(item)
        }

        return isAdded
    }

    fun filterOwner(item: Item): Boolean{

        var isAdded = false

        when (item.owner1){
            100 -> isAdded = filterOwner1
            in 1..99 -> isAdded = filterShared
        }

        when (item.owner2){
            100 -> isAdded = filterOwner2
            in 1..99 -> isAdded = filterShared
        }

        return isAdded
    }

    private fun filterStatus(item: Item): Boolean{
        var isAdded = false
        filterStatusList.forEach {
            if (it.contentEquals(item.status,true)) {
                isAdded = true
            } else if (it == "No informado" && item.status.isNullOrEmpty() ){
                isAdded = true
            }
        }
        return isAdded
    }

    private fun filterType(item: Item): Boolean{
        var isAdded = false
        filterTypeList.forEach {
            if (it.toString().equals(item.type,true)) {
                isAdded = true
            }
        }
        return isAdded
    }

    fun getFilterStatus(type: String): Boolean {
        return when (type) {
            OWNER_1 -> filterOwner1
            OWNER_2 -> filterOwner2
            SOCIEDAD -> filterShared
            else -> filterStatusList.contains(type)
        }
    }

    fun getIsOwnerFiltered(): Boolean{
        return filterOwner1 || filterOwner2 || filterShared
    }

    fun setFilter(type: String) {

        if (type != OWNER_1 && type != OWNER_2 && type != SOCIEDAD) {
            if( type != "Mecánica" && type != "Hidraúlica" && type != "Neumática") {
                if (filterStatusList.contains(type.uppercase())) {
                    filterStatusList -= type.uppercase(Locale.getDefault())
                } else {
                    filterStatusList += type.uppercase(Locale.getDefault())
                }
            } else {
                val char = type[0].toString()
                if (filterTypeList.contains(char.uppercase())) {
                    filterTypeList -= char.uppercase()
                } else {
                    filterTypeList += char.uppercase()
                }
            }
        } else {
            when (type) {
                OWNER_1 -> filterOwner1 = !filterOwner1
                OWNER_2 -> filterOwner2 = !filterOwner2
                SOCIEDAD -> filterShared = !filterShared
            }
        }

        Log.d(TAG, "FilterStatusList -> $filterStatusList")
        Log.d(TAG, "FilterTypeList -> $filterTypeList")
        isFilteredList = setIsFilteredList()
    }

    private fun setIsFilteredList(): Boolean {
        return filterOwner1 || filterOwner2 || filterShared || filterStatusList.isNotEmpty() || filterTypeList.isNotEmpty()
    }

    fun setQueryText(inputSearch: String?) {
        searchInput = if (inputSearch != null) {
            "%$inputSearch%"
        } else {
            "%%"
        }
    }

    fun getSearchInput(): String = searchInput

    fun setProduct(newProduct: String) {
        product = newProduct
    }

    fun getProduct(): String = product

    fun clearFilters() {

        isFilteredList = false

        filterOwner1 = false
        filterOwner2 = false
        filterShared = false

        filterStatusList.clear()

        filterTypeList.clear()

    }

    companion object {
        const val TAG = "CustomListUtil"
    }


}


