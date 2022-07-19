package com.ferpa.machinestock.data

import android.os.Build.VERSION_CODES.M
import android.util.Log
import androidx.annotation.WorkerThread
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.network.ItemsApi
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.Exception

class ItemRepository
constructor(
    private val itemDao: ItemDao,
    private val itemsApi: ItemsApi,
    val customListUtil: CustomListUtil
) {

    var allItems = itemDao.getAll()

    val itemsFlow: Flow<List<Item>> = getCustomQuery()

    private fun getCustomQuery() = flow<List<Item>> {

        val flow =
            if (customListUtil.getSearchInput() == "%%") {
                if (customListUtil.getProduct() == "TODAS" && customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isEmpty()) {
                    itemDao.getAll()
                } else if (customListUtil.filterStatusList.isNotEmpty()) {
                    if (customListUtil.filterTypeList.isNotEmpty()) {
                        itemDao.getFilteredProductStatusAndType(
                            customListUtil.getProduct(),
                            customListUtil.filterStatusList,
                            customListUtil.filterTypeList
                        )
                    } else {
                        itemDao.getFilteredProductAndStatus(
                            customListUtil.getProduct(),
                            customListUtil.filterStatusList
                        )
                    }
                } else if (customListUtil.filterTypeList.isNotEmpty()) {
                    itemDao.getFilteredProductAndType(
                        customListUtil.getProduct(),
                        customListUtil.filterTypeList
                    )
                } else {
                    itemDao.getFilteredProduct(
                        customListUtil.getProduct()
                    )
                }
            } else {
                if (!customListUtil.isFilteredList && customListUtil.getProduct() == "TODAS") {
                    itemDao.getSearchQueryAllItems(
                        customListUtil.getSearchInput()
                    )
                } else if (customListUtil.filterStatusList.isNotEmpty()) {
                    if (customListUtil.filterTypeList.isNotEmpty()) {
                        itemDao.getFilteredProductStatusAndTypeWithSearch(
                            customListUtil.getProduct(),
                            customListUtil.filterStatusList,
                            customListUtil.filterTypeList,
                            customListUtil.getSearchInput()
                        )
                    } else {
                        itemDao.getFilteredProductAndStatusWithSearch(
                            customListUtil.getProduct(),
                            customListUtil.filterStatusList,
                            customListUtil.getSearchInput()
                        )
                    }
                } else if (customListUtil.filterTypeList.isNotEmpty()) {
                    itemDao.getFilteredProductAndTypeWithSearch(
                        customListUtil.getProduct(),
                        customListUtil.filterTypeList,
                        customListUtil.getSearchInput()
                    )
                } else {
                    itemDao.getFilteredProductWithSearch(
                        customListUtil.getProduct(),
                        customListUtil.getSearchInput()
                    )
                }
            }

        if (customListUtil.getIsOwnerFiltered()) {

        }

        emit(flow.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilterList(flowList: Flow<List<Item>>): Flow<List<Item>> {
        return flowList.mapLatest { list ->
            list.filter { customListUtil.filterItem(it) }.sortedBy { it.feature1 }
        }
    }

    /*
    Main Menu
     */
    val menuList: Flow<List<MainMenuItem>> = getMenuItemList()

    private fun getMenuItemList() = flow<List<MainMenuItem>> {

        var menuList = arrayListOf<MainMenuItem>()
        MainMenuSource.mainMenu.forEach() {
            menuList.add(createMenuItem(it))
        }

        emit(menuList)
    }

    private suspend fun createMenuItem(mainMenuItem: MainMenuItem): MainMenuItem {

        return MainMenuItem(
            mainMenuItem.name,
            mainMenuItem.imageResourceId,
            mainMenuItem.priority,
            mainMenuItem.hasNews,
            mainMenuItem.menuListUtil,
            getMenuQuery(mainMenuItem.menuListUtil)
        )
    }

    private suspend fun getMenuQuery(menuListUtil: MenuListUtil): List<Item> {

        return if (menuListUtil.filterByProduct.size == 1) {
            val product = menuListUtil.filterByProduct.first()
            itemDao.getProductsWithLimit(product, menuListUtil.listSize.toString())
                .map { list ->
                    menuListUtil.getMenuList(list)
                }.first()
        } else if (menuListUtil.filterByProduct.size > 1 && menuListUtil.filterByProduct.contains(
                "NOT"
            )
        ) {
            val notProduct = menuListUtil.filterByProduct.subList(1, 5)
            itemDao.getOthersProductsWithLimit(
                notProduct[0],
                notProduct[1],
                notProduct[2],
                notProduct[3],
                menuListUtil.listSize.toString()
            ).map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        } else {
            allItems.map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        }
    }

    /*
    Custom List Util Parameters
     */
    fun setProduct(newProduct: String) {
        customListUtil.setProduct(newProduct)
        Log.d(TAG, "New Filter Product -> $newProduct")
    }

    fun setQueryText(inputSearch: String?) {
        customListUtil.setQueryText(inputSearch)
        Log.d(TAG, "Query Text -> $inputSearch")
    }

    fun getFilterStatus(type: String): Boolean {
        return customListUtil.getFilterStatus(type)
    }

    fun setFilter(type: String) {
        customListUtil.setFilter(type)
        Log.d(TAG, "Filter Type -> $type")
    }

    fun isFilterList(): Boolean {
        return customListUtil.isFilteredList
    }

    fun clearFilters() {
        customListUtil.clearFilters()
    }

    /*
    Single Item
     */
    fun getItem(id: Long): Flow<Item> {
        return itemDao.getItem(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertItem(item: Item) {
        itemDao.insert(item)
    }

    suspend fun postItem(item: Item) {
        try {
            itemsApi.postNewItem(item)
        } catch (e: Exception) {
            Log.d("PostItem", e.toString())
            //TODO Save action to try it again later
        }
    }

    suspend fun updateItem(item: Item) {
        itemDao.update(item)
        try {
            itemsApi.updateItem(item)
        } catch (e: Exception) {
            Log.d("UpdateItem", e.toString())
            //TODO Save action to try it again later
        }
    }


    /*
    Network
     */
    suspend fun compareDatabases() {
        /*
        val getItemsFromNetwork = itemsApi.getAllItems()
        var needUpdate = true
        if (getItemsFromNetwork.isSuccessful) {
        itemDao.getAll().collectLatest { localList ->
        while (needUpdate) {
        getItemsFromNetwork.body()?.forEach { networkItem ->
        val currentItem = localList.find { localItem ->
        networkItem.id == localItem.id
        }
        if (currentItem == null) {
        itemDao.insert(networkItem)
        Log.d("NetworkTrack", "Insert Item: ${networkItem.id}")
        } else {
        if (networkItem.editDate!! > currentItem.editDate.toString()) {
        itemDao.update(networkItem)
        Log.d("NetworkTrack", "Update Item: ${networkItem.id}")
        } else {
        Log.d("NetworkTrack", "Update End")
        needUpdate = false
        return@collectLatest
        }
        }
        }
        }
        }
        }
         */
    }

    //TODO Delete when this will not more necessary
    suspend fun populateDb() {
        allItems.collect() { list ->
            list.forEach { item ->
                try {
                    itemsApi.postNewItem(item)
                } catch (e: Exception) {
                    Log.d("PostItem", e.toString())
                }
            }
        }
    }

    companion object {
        const val TAG = "ItemRepository"
    }

}


