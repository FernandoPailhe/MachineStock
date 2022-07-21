package com.ferpa.machinestock.data


import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.network.ItemsApi
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import kotlinx.coroutines.flow.*
import kotlin.Exception

class ItemRepository
constructor(
    private val itemDao: ItemDao,
    private val itemsApi: ItemsApi,
    val customListUtil: CustomListUtil
) {

    private val dbSize = if (itemDao.getAll().asLiveData().value != null) itemDao.getAll()
        .asLiveData().value!!.size else 200

    private val allItems = itemDao.getAll()

    val productArray = itemDao.getProductList()

    val itemsFlow: Flow<List<Item>> = getCustomQuery()

    //TODO add filter owner
    /*
    Custom List Queries
     */
    private fun getCustomQuery() = flow {

        val productList = listOf(customListUtil.getProduct())

        val flow =
            if (customListUtil.getSearchInput() == "%%") {
                if (customListUtil.getProduct() == "TODAS") {
                    if (customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isEmpty()) {
                        itemDao.getAll()
                    } else if (customListUtil.filterTypeList.isNotEmpty() && customListUtil.filterStatusList.isEmpty()) {
                        if (customListUtil.getIsOwnerFiltered()) {
                            itemDao.getAllFilterTypeAndOwner(
                                customListUtil.filterTypeList,
                                customListUtil.filterOwnerList,
                                dbSize.toString()
                            )
                        } else {
                            itemDao.getAllFilterType(
                                customListUtil.filterTypeList,
                                dbSize.toString()
                            )
                        }
                    } else if (customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isNotEmpty()) {
                        if (customListUtil.getIsOwnerFiltered()) {
                            itemDao.getAllFilterStatusAndOwner(
                                customListUtil.filterStatusList,
                                customListUtil.filterOwnerList,
                                dbSize.toString()
                            )
                        } else {
                            itemDao.getAllFilterStatus(
                                customListUtil.filterStatusList,
                                dbSize.toString()
                            )
                        }
                    } else {
                        if (customListUtil.getIsOwnerFiltered()) {
                            itemDao.getAllFilterStatusOwnerAndType(
                                customListUtil.filterStatusList,
                                customListUtil.filterOwnerList,
                                customListUtil.filterTypeList,
                                dbSize.toString()
                            )
                        } else {
                            itemDao.getAllFilterStatusAndType(
                                customListUtil.filterStatusList, customListUtil.filterTypeList,
                                dbSize.toString()
                            )
                        }
                    }
                } else if (customListUtil.filterStatusList.isNotEmpty()) {
                    if (customListUtil.filterTypeList.isNotEmpty()) {
                        itemDao.getFilteredProductStatusAndType(
                            productList,
                            customListUtil.filterStatusList,
                            customListUtil.filterTypeList,
                            dbSize.toString()
                        )
                    } else {
                        itemDao.getFilteredProductAndStatus(
                            productList,
                            customListUtil.filterStatusList,
                            dbSize.toString()
                        )
                    }
                } else if (customListUtil.filterTypeList.isNotEmpty()) {
                    itemDao.getFilteredProductAndType(
                        productList,
                        customListUtil.filterTypeList,
                        dbSize.toString()
                    )
                } else {
                    itemDao.getFilteredProduct(
                        productList,
                        dbSize.toString()
                    )
                }
            } else {
                if (customListUtil.getProduct() == "TODAS") {
                    if (!customListUtil.isFilteredList) {
                        itemDao.getSearchQueryAllItems(
                            customListUtil.getSearchInput()
                        )
                    } else if (customListUtil.filterTypeList.isNotEmpty() && customListUtil.filterStatusList.isEmpty()) {
                        itemDao.getAllFilterTypeWithSearch(
                            customListUtil.filterTypeList,
                            customListUtil.getSearchInput()
                        )
                    } else if (customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isNotEmpty()) {
                        itemDao.getAllFilterStatusWithSearch(
                            customListUtil.filterStatusList,
                            customListUtil.getSearchInput()
                        )
                    } else {
                        itemDao.getAllFilterStatusAndTypeWithSearch(
                            customListUtil.filterStatusList, customListUtil.filterTypeList,
                            customListUtil.getSearchInput()
                        )
                    }
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

        emit(flow.first())

    }

    /*
    Main Menu
     */
    val menuList: Flow<List<MainMenuItem>> = getMenuItemList()

    private fun getMenuItemList() = flow<List<MainMenuItem>> {

        val menuList = arrayListOf<MainMenuItem>()
        MainMenuSource.mainMenu.forEach {
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

        //TODO implement different orders
        return if (menuListUtil.filterByProduct.isNotEmpty()) {
            if (menuListUtil.filterByProduct.contains("NOT")) {
                itemDao.getFilteredNotProductAndStatus(
                    menuListUtil.filterByProduct,
                    menuListUtil.filterByStatus,
                    menuListUtil.listSize.toString()
                ).first()
            } else if (menuListUtil.filterByStatus.isNotEmpty()) {
                itemDao.getFilteredProductAndStatus(
                    menuListUtil.filterByProduct,
                    menuListUtil.filterByStatus,
                    menuListUtil.listSize.toString()
                ).first()
            } else {
                itemDao.getFilteredProduct(
                    menuListUtil.filterByProduct,
                    menuListUtil.listSize.toString()
                ).first()
            }
        } else if (menuListUtil.filterByStatus.isNotEmpty()) {
            itemDao.getAllFilterStatus(
                menuListUtil.filterByStatus,
                menuListUtil.listSize.toString()
            ).first()
        } else {
            if (menuListUtil.sortBy == "editDate") {
                itemDao.getAll().map {
                    menuListUtil.getMenuList(it)
                }.first()
            } else {
                itemDao.getAllWithLimitSortByInsertDate(menuListUtil.listSize.toString())
                    .first()
            }
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
        allItems.collect { list ->
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




