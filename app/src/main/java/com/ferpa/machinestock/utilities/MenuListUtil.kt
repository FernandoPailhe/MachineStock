package com.ferpa.machinestock.utilities

import android.util.Log
import com.ferpa.machinestock.model.Item

class MenuListUtil(
    var sortBy: String? = null,
    var filterByStatus: List<String> = listOf(),
    var filterByProduct: List<String> = listOf(),
    var listSize: Int = Const.MENU_LIST_SIZE,
) {

    fun getMenuList(itemList: List<Item>): List<Item> {
        itemList.map {
            filterItem(it)
        }

        return sortList(itemList)
    }

    private fun filterItem(item: Item): Boolean {

        var isAdded = false

        if (filterByStatus.isNotEmpty()) {
            isAdded = filterStatus(item)
        }

        return isAdded
    }

    private fun sortList(list: List<Item>): List<Item> {

        Log.d(TAG, "${this.sortBy}")

        return if (list.isNotEmpty()) {

            val size = if (list.size > this.listSize) this.listSize else list.size

            return if (this.sortBy.equals("insertDate")) {
                list.sortedByDescending { it.insertDate }
                .subList(0, size)
            } else if (this.sortBy.equals("editDate")) {
                list.filterNot { item ->
                    item.editDate.equals(item.insertDate)
                }
                    .sortedByDescending { it.editDate }
            } else {
                list.sortedByDescending { it.feature1 }.subList(0, size)
            }
        } else {
            list
        }

    }

    private fun filterStatus(item: Item): Boolean {
        var isAdded = false
        filterByStatus.forEach {
            if (it.contentEquals(item.status, true)) {
                isAdded = true
            } else if (it == "No informado" && item.status.isNullOrEmpty()) {
                isAdded = true
            }
        }
        return isAdded
    }

    private fun filterType(item: Item): Boolean {
        var isAdded = false
        filterByProduct.forEach {
            if (it.equals(item.type, true)) {
                isAdded = true
            }
        }
        return isAdded
    }

    companion object {
        const val TAG = "MenuListUtil"
    }

}