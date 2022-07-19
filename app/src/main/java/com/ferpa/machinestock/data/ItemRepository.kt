package com.ferpa.machinestock.data


import android.util.Log
import androidx.annotation.WorkerThread
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

    private var allItems = itemDao.getAll()

    val itemsFlow: Flow<List<Item>> = getCustomQuery()

    private fun getCustomQuery() = flow {

        val flow =
            if (customListUtil.getSearchInput() == "%%") {
                if (customListUtil.getProduct() == "TODAS"){
                    if ( customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isEmpty()) {
                        itemDao.getAll()
                    } else if (customListUtil.filterTypeList.isNotEmpty() && customListUtil.filterStatusList.isEmpty()){
                        itemDao.getAllFilterType(customListUtil.filterTypeList)
                    } else if (customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isNotEmpty()){
                        itemDao.getAllFilterStatus(customListUtil.filterStatusList)
                    } else {
                        itemDao.getAllFilterStatusAndType(customListUtil.filterStatusList, customListUtil.filterTypeList)
                    }
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
                if (customListUtil.getProduct() == "TODAS") {
                    if (!customListUtil.isFilteredList ) {
                        itemDao.getSearchQueryAllItems(
                            customListUtil.getSearchInput()
                        )
                    } else if (customListUtil.filterTypeList.isNotEmpty() && customListUtil.filterStatusList.isEmpty()){
                        itemDao.getAllFilterTypeWithSearch(
                            customListUtil.filterTypeList,
                            customListUtil.getSearchInput()
                        )
                    } else if (customListUtil.filterTypeList.isEmpty() && customListUtil.filterStatusList.isNotEmpty()){
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

        return if (menuListUtil.filterByProduct.size == 1) {
            val product = menuListUtil.filterByProduct.first()
            itemDao.getProductsWithLimit(product, menuListUtil.listSize.toString())
                .map { list ->
                    menuListUtil.getMenuList(list)
                }.first()
        } else if (menuListUtil.filterByProduct.size > 1 && menuListUtil.filterByProduct.contains("NOT")) {
            val notProduct = menuListUtil.filterByProduct.subList(1, 5)
            itemDao.getOthersProductsWithLimit(notProduct,
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




