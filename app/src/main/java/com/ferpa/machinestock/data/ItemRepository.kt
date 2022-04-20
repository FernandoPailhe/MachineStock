package com.ferpa.machinestock.data

import android.util.Log
import androidx.annotation.WorkerThread
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.network.ItemsApi
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.lang.Exception


class ItemRepository
constructor(
    private val itemDao: ItemDao,
    private val itemsApi: ItemsApi,
    val customListUtil: CustomListUtil
) {

    /**
    suspend fun getItems(): Flow<DataState<List<Item>>> = flow {
        emit(DataState.Loading)
        try {
            val networkItems = itemsApi.getTestItem()
        } catch (e: Exception){
            emit(DataState.Error(e))
        }

    }
    **/

    var allItems = itemDao.getAll()

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsFlow: Flow<List<Item>> = getCustomQuery().flatMapLatest {
        getCustomQuery()
    }


    //TODO Migrate this logic to the future server?
    fun getCustomQuery(): Flow<List<Item>> {
        val product = customListUtil.getProduct()
        val searchQuery = customListUtil.getSearchInput()
        var temporalQuery = allItems
        if (product != "TODAS") {
            if (customListUtil.getSearchInput() == "%%") {
                temporalQuery = itemDao.getProducts(product)
                Log.d("FLOWQUERY", product)
            } else {
                temporalQuery = itemDao.getSearchQuery(product, searchQuery)
                Log.d("FLOWQUERY", searchQuery)
            }
        } else if (customListUtil.getSearchInput() != "%%") {
            temporalQuery = itemDao.getSearchQueryAll(customListUtil.getSearchInput())
        }
        Log.d("FLOWQUERY", searchQuery)

        if (customListUtil.isFilteredList) {
            temporalQuery = getFilterList(temporalQuery)
        }

        return (temporalQuery)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilterList(flowList: Flow<List<Item>>): Flow<List<Item>> {
        return flowList.mapLatest { list ->
            list.filter { customListUtil.filterItem(it) }.sortedBy { it.price }
        }
    }

    fun getItem(id: Int): Flow<Item> {
        return itemDao.getItem(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertItem(item: Item) {
        itemDao.insert(item)
    }

    suspend fun updateItem(item: Item) {
        itemDao.update(item)
    }

    /**
    private val _state = MutableStateFlow<ItemState>(ItemState())
    val state: StateFlow<ItemState> = _state


    suspend fun getTestItem() {
        try {
            _state.value.isLoading = true
            _state.value = state.value.copy(
                item = itemsApi.getTestItem(),
                isLoading = false
            )
        } catch (e: Exception) {
            Log.e("itemsApi", "getTestItem: ", e)
            _state.value = state.value.copy(isLoading = false)
        }
    }

    data class ItemState(
        val item: Item? = null,
        var isLoading: Boolean = false
    )
    **/

    /**
    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ItemRepository? = null

        fun getInstance(itemDao: ItemDao, customListUtil: CustomListUtil) =
            instance ?: synchronized(this) {
                instance ?: ItemRepository(itemDao, customListUtil).also { instance = it }
            }
    }
    **/
}


