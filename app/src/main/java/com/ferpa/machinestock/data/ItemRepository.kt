package com.ferpa.machinestock.data


import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.network.ItemsApi
import com.ferpa.machinestock.utilities.Const.USED_FIRESTORE_DB
import com.ferpa.machinestock.utilities.Const.USERS_FIRESTORE_DB
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.Exception

class ItemRepository
constructor(
    private val itemDao: ItemDao,
    val customListUtil: CustomListUtil
) {

    private val dbSize = if (itemDao.getAll().asLiveData().value != null) itemDao.getAll()
        .asLiveData().value!!.size else 200

    private val allItems = itemDao.getAll()

    val isLocalDbUpdated = MutableStateFlow(false)

    private val lastUpdate = itemDao.getLastUpdate()

    private val lastInsertDate = itemDao.getLastNewItem()

    private val firestoreUsedDbRef = Firebase.firestore.collection(USED_FIRESTORE_DB)

    private val firestoreUsersDbRef = Firebase.firestore.collection(USERS_FIRESTORE_DB)

    val productArray = itemDao.getProductList()

    val itemsFlow: Flow<List<Item>> = getCustomQuery()

    //TODO add filter owner
    /*
    Custom List Queries
     */
    private fun getCustomQuery() = flow {
        //TODO check if this work fine
        compareLastNewItem()
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
        MainMenuSource.mainMenu.sortedBy{it.priority}.forEach {
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
        postItem(item)
        itemDao.insert(item)
    }

    suspend fun updateStatus(item: Item) {
        postUpdateStatus(item)
        itemDao.update(item)
    }

    suspend fun updateItem(item: Item) {
        postItem(item)
        itemDao.update(item)
    }

    /*
    Network
     */
    suspend fun compareLastNewItem() {
        try {
            firestoreUsedDbRef
                .whereGreaterThan("insertDate", lastInsertDate.first().toString())
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Retrieve ${it.documents.size} new items")
                    if (it.documents.size > 0 ) {
                        isLocalDbUpdated.value = false
                        for (itemFirestore in it.documents) {
                            Log.d(TAG, "Retrieve ${itemFirestore.toObject<Item>()}")
                            CoroutineScope(Dispatchers.IO).launch {
                                itemFirestore.toObject<Item>()?.let { it1 ->
                                    itemDao.insert(it1)
                                    Log.d(TAG, "Create new ${itemFirestore.toObject<Item>()}")
                                }
                            }
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        compareLastEdit()
                    }
                }
                .addOnFailureListener { }
        } catch (e: Exception) {

        }
    }

    private suspend fun compareLastEdit() {
        try {
            firestoreUsedDbRef
                .whereGreaterThan("editDate", lastUpdate.first().toString())
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Retrieve ${it.documents.size} edited items")
                    if (it.documents.size > 0 ) {
                        isLocalDbUpdated.value = false
                        for (itemFirestore in it.documents) {
                            Log.d(TAG, "Retrieve ${itemFirestore.toObject<Item>()}")
                            CoroutineScope(Dispatchers.IO).launch {
                                itemFirestore.toObject<Item>()?.let { it1 ->
                                    itemDao.update(it1)
                                    Log.d(TAG, "Update ${itemFirestore.toObject<Item>()}")
                                }
                            }
                        }
                    }
                    isLocalDbUpdated.value = true
                }.addOnFailureListener {

                }
        } catch (e: Exception) {

        }

    }

    private fun postItem(item: Item) = CoroutineScope(Dispatchers.IO).launch {
        try {
            firestoreUsedDbRef.document(item.id.toString()).set(item).addOnSuccessListener {
                Log.d(TAG, "Post ${item.id} Succes")
            }.addOnFailureListener { e ->
                Log.d(TAG, "PostItem in Firestore -> $e")
            }
        } catch (e: Exception) {
            Log.d(TAG, "PostItem in Firestore -> $e")
        }
    }

    private fun postUpdateStatus(item: Item) = CoroutineScope(Dispatchers.IO).launch {
        try {
            firestoreUsedDbRef.document(item.id.toString())
                .update(
                    "status", item.status,
                    "editDate", item.editDate,
                    "editUser", item.editUser
                )
                .addOnSuccessListener {
                    Log.d(TAG, "${item.id} New Status -> ${item.status} Succes")
                }.addOnFailureListener { e ->
                    Log.d(TAG, "NewStatus in Firestore -> $e")
                }
        } catch (e: Exception) {
            Log.d(TAG, "NewStatus in Firestore -> $e")
        }
    }

    fun retrieveItem(id: Long) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot =
                firestoreUsedDbRef.document(id.toString()).get().addOnSuccessListener {
                    val item = it.toObject<Item>()
                }.addOnFailureListener {
                    Log.d(TAG, "Retrieve Item $id in Firestore -> $it")
                }
        } catch (e: Exception) {

        }
    }

    private fun subscribeToRealtimeUpdates() {
        firestoreUsedDbRef.addSnapshotListener { querySnapshot, firestoreException ->
            firestoreException?.let {
                Log.d(TAG, it.message.toString())
            }
            querySnapshot?.let {

            }
        }
    }

    //TODO move user logic to another class
    fun createNewUser(uid: String, userMap: Map<String, String> ) = CoroutineScope(Dispatchers.IO).launch{
        try {
            firestoreUsersDbRef.document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d(TAG, "$uid New User -> ${userMap.values} Succes")
                }.addOnFailureListener { e ->
                    Log.d(TAG, "NewStatus in Firestore -> $e")
                }
        } catch (e: Exception) {
            Log.d(TAG, "NewUser in Firestore -> $e")
        }

    }

    companion object {
        const val TAG = "ItemRepository"
    }
}




