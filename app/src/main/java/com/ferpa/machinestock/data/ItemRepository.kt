package com.ferpa.machinestock.data

import android.util.Log
import androidx.annotation.WorkerThread
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.network.ItemsApi
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.Exception

const val TAG = "ItemRepository"

class ItemRepository
constructor(
    private val itemDao: ItemDao,
    private val itemsApi: ItemsApi,
    val customListUtil: CustomListUtil
) {

    var allItems = itemDao.getAll()


    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsFlow: Flow<List<Item>> = getCustomQuery().flatMapLatest {
        getCustomQuery()
    }

    //val itemsFlow: Flow<List<Item>> = getCustomQuery()

    fun getCustomQuery(): Flow<List<Item>> {
        val product = customListUtil.getProduct()
        val searchQuery = customListUtil.getSearchInput()
        var temporalQuery = allItems
        if (product != "TODAS") {
            if (customListUtil.getSearchInput() == "%%") {
                temporalQuery = itemDao.getProducts(product, "Vendida", "Retirada")
            } else {
                temporalQuery = itemDao.getSearchQueryFilterByProduct(product, searchQuery)
            }
        } else if (customListUtil.getSearchInput() != "%%") {
            temporalQuery = itemDao.getSearchQueryAllItems(customListUtil.getSearchInput())
        }

        if (customListUtil.isFilteredList) {
            temporalQuery = getFilterList(temporalQuery)
        }

        return (temporalQuery)

    }

    val menuList: Flow<List<MainMenuItem>> = getMenuItemList()

    private fun getMenuItemList() = flow<List<MainMenuItem>> {

        var menuList = arrayListOf<MainMenuItem>()
        MainMenuSource.mainMenu.forEach() {
            menuList.add(createMenuItem(it))
            Log.d(TAG, it.name)
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
            itemDao.getProductsWithLimit(product, menuListUtil.listSize.toString()).map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        } else if (menuListUtil.filterByProduct.size > 1 && menuListUtil.filterByProduct.contains("NOT")) {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilterList(flowList: Flow<List<Item>>): Flow<List<Item>> {
        return flowList.mapLatest { list ->
            list.filter { customListUtil.filterItem(it) }.sortedBy { it.feature1 }
        }
    }

    fun getItem(id: Long): Flow<Item> {
        return itemDao.getItem(id)
    }

    suspend fun compareDatabases() {
        /**
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
         **/
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

}


