package com.ferpa.machinestock.utilities

import com.ferpa.machinestock.model.Item

class CustomListUtil {

    private var product: String = "TODAS"

    var isFilteredList: Boolean = false

    private var searchInput: String = "%%"

    private var filterOwner1: Boolean = false

    private var filterOwner2: Boolean = false

    private var filterShared: Boolean = false

    private val filterStatusList: MutableList<String> = mutableListOf()

    private val filterTypeList: MutableList<Char> = mutableListOf()

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

    private fun filterOwner(item: Item): Boolean{

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
            "Maquimundo" -> filterOwner1
            "Canavese" -> filterOwner2
            "Sociedad" -> filterShared
            else -> filterStatusList.contains(type)
        }
    }

    fun setFilter(type: String) {

        if (type != "Maquimundo" && type != "Canavese" && type != "Sociedad") {
            if( type != "Mecánica" && type != "Hidraúlica" && type != "Neumática") {
                if (filterStatusList.contains(type)) {
                    filterStatusList -= type
                } else {
                    filterStatusList += type
                }
            } else {
                val char: Char = type[0]
                if (filterTypeList.contains(char)) {
                    filterTypeList -= char
                } else {
                    filterTypeList += char
                }
            }
        } else {
            when (type) {
                "Maquimundo" -> filterOwner1 = !filterOwner1
                "Canavese" -> filterOwner2 = !filterOwner2
                "Sociedad" -> filterShared = !filterShared
            }
        }

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


}


