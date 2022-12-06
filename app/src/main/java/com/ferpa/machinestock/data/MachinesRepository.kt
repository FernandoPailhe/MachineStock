package com.ferpa.machinestock.data


import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import com.ferpa.machinestock.data.MachinesRepository.Companion.FIRST_TIME
import com.ferpa.machinestock.data.MachinesRepository.Companion.WITH_PRICE
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.model.MachineStockUser
import com.ferpa.machinestock.model.MainMenuPreferences
import com.ferpa.machinestock.utilities.Const
import com.ferpa.machinestock.utilities.Const.MACHINES_FIRESTORE_DB
import com.ferpa.machinestock.utilities.Const.USERS_FIRESTORE_DB
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.Exception
import kotlin.random.Random

const val DATA_STORE_NAME = "user_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class MachinesRepository
constructor(
    private val machineDao: MachineDao,
    val customListUtil: CustomListUtil,
    private val appContext: Context
) {

    companion object {
        const val TAG = "ItemRepository"
        val FIRST_TIME = booleanPreferencesKey("firstTime")
        val PREFS_VERSION = intPreferencesKey("prefsVersion")
        val WITH_PRICE = booleanPreferencesKey("withPrice")
    }

    private val dbSize = if (machineDao.getAll().asLiveData().value != null) machineDao.getAll()
        .asLiveData().value!!.size else 200

    private val allItems = machineDao.getAll()

    val isLocalDbUpdated = MutableStateFlow(false)

    private val lastUpdate = machineDao.getLastUpdate()

    private val lastInsertDate = machineDao.getLastNewMachine()

    private val firestoreMachinesDbRef = Firebase.firestore.collection(MACHINES_FIRESTORE_DB)

    private val firestoreUsersDbRef = Firebase.firestore.collection(USERS_FIRESTORE_DB)

    val mainMenuPreferencesList = machineDao.getMainMenuPreferencesList()

    val productArray = machineDao.getProductList()

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
                temporalQuery =
                    machineDao.getProducts(product, listOf("Vendida", "Retirada", "Eliminada"))
            } else {
                temporalQuery = machineDao.getSearchQueryFilterByProduct(product, searchQuery)
            }
        } else if (customListUtil.getSearchInput() != "%%") {
            temporalQuery = machineDao.getSearchQueryAllItems(customListUtil.getSearchInput())
        }

        if (customListUtil.isFilteredList) {
            temporalQuery = getFilterList(temporalQuery)
        }

        return (temporalQuery)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilterList(flowList: Flow<List<Item>>): Flow<List<Item>> {
        return if (customListUtil.getProduct() == "TODAS") {
            flowList.mapLatest { list ->
                list.filter { customListUtil.filterItem(it) }
            }
        } else {
            flowList.mapLatest { list ->
                list.filter { customListUtil.filterItem(it) }.sortedBy { it.feature1 }
            }
        }
    }

    /*
    Data Store Preferences
     */
    private fun getFirstTime() = appContext.dataStore.data.map {
        Log.d(TAG, "$FIRST_TIME value -> ${it[FIRST_TIME]}")
        it[FIRST_TIME]
    }

    private suspend fun setFirstTime(isFirstTime: Boolean) {
        appContext.dataStore.edit {
            it[FIRST_TIME] = isFirstTime
            it[PREFS_VERSION] = Const.PREFS_VERSION
            Log.d(TAG, "New $FIRST_TIME value -> ${it[FIRST_TIME]}")
            Log.d(TAG, "$PREFS_VERSION value -> ${it[PREFS_VERSION]}")
        }
    }

    fun getWithPrice() = appContext.dataStore.data.map {
        if (it[WITH_PRICE] == null) {
            setWithPrice(false)
            false
        } else {
            it[WITH_PRICE]
        }
    }

    private suspend fun setWithPrice(shareWithPrice: Boolean) {
        appContext.dataStore.edit {
            it[WITH_PRICE] = shareWithPrice
        }
    }

    /*
    Main Menu
     */
    val menuList: Flow<List<MainMenuItem>> = getMenuItemList()

    private fun getMenuItemList() = flow<List<MainMenuItem>> {

        if (getFirstTime().first() == null) {
            createMainMenuPreference()
            setFirstTime(false)
        }

        val menuList = arrayListOf<MainMenuItem>()
        MainMenuSource.mainMenu.sortedBy { it.priority }.forEach {
            menuList.add(createMenuItem(it))
        }

        emit(menuList.toList())
    }

    private suspend fun createMenuItem(mainMenuItem: MainMenuItem): MainMenuItem {

        val preferences = machineDao.getMainMenuPreferences(mainMenuItem.name).first()

        return if (preferences != null) {
            MainMenuItem(
                mainMenuItem.name,
                mainMenuItem.imageResourceId,
                preferences.priority,
                mainMenuItem.hasNews,
                mainMenuItem.menuListUtil,
                getMenuQuery(mainMenuItem.menuListUtil),
                preferences.initiallyExpanded,
                preferences.visible
            )
        } else {
            MainMenuItem(
                mainMenuItem.name,
                mainMenuItem.imageResourceId,
                mainMenuItem.priority,
                mainMenuItem.hasNews,
                mainMenuItem.menuListUtil,
                getMenuQuery(mainMenuItem.menuListUtil),
                mainMenuItem.initiallyExpanded,
                mainMenuItem.visible
            )
        }
    }

    private suspend fun getMenuQuery(menuListUtil: MenuListUtil): List<Item> {

        val notStatus = listOf("Retirada", "Vendida")

        return if (menuListUtil.filterByProduct.size == 1) {
            val product = menuListUtil.filterByProduct.first()
            machineDao.getProducts(product, notStatus).map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        } else if (menuListUtil.filterByProduct.size > 1 && menuListUtil.filterByProduct.contains("NOT")) {
            val notProduct = menuListUtil.filterByProduct.subList(1, 5)
            machineDao.getOthersProductsWithLimit(
                notProduct,
                menuListUtil.listSize.toString()
            ).map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        } else {
            allItems.map { list ->
                menuListUtil.getMenuList(list)
            }.first()
        }
        /*
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

         */
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
        return machineDao.getMachine(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertItem(item: Item) {
        postItem(item)
        machineDao.insert(item)
    }

    suspend fun updateStatus(item: Item) {
        postUpdateStatus(item)
        machineDao.update(item)
    }

    suspend fun updateItem(item: Item) {
        postItem(item)
        machineDao.update(item)
    }

    /*
    Network
     */
    private suspend fun compareLastNewItem() {
        var lastNewItem = lastInsertDate.first()
        if (lastNewItem == null) {
            lastNewItem = "0"
        }
        Log.d("LastInsertDate", lastNewItem)
        try {
            firestoreMachinesDbRef
                .whereGreaterThan("insertDate", lastNewItem)
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Retrieve ${it.documents.size} new items")
                    if (it.documents.size > 0) {
                        isLocalDbUpdated.value = false
                        for (itemFirestore in it.documents) {
                            Log.d(TAG, "Retrieve ${itemFirestore.toObject<Item>()}")
                            CoroutineScope(Dispatchers.IO).launch {
                                itemFirestore.toObject<Item>()?.let { it1 ->
                                    machineDao.insert(it1)
                                    Log.d(TAG, "Create new ${itemFirestore.toObject<Item>()}")
                                }
                            }
                        }
                    }
                    isLocalDbUpdated.value = true
                    Log.d(TAG, "isLocalDbUpdated ${isLocalDbUpdated.value.toString()}")
                }
                .addOnFailureListener { }
        } catch (e: Exception) {

        }
    }

    private suspend fun compareLastEdit() {
        var lastEditDate = lastUpdate.first()
        if (lastEditDate == null) {
            lastEditDate = "0"
        }
        Log.d("LastEditDate", lastEditDate)
        try {
            firestoreMachinesDbRef
                .whereGreaterThan("editDate", lastEditDate)
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Retrieve ${it.documents.size} edited items")
                    if (it.documents.size > 0) {
                        isLocalDbUpdated.value = false
                        for (itemFirestore in it.documents) {
                            Log.d(TAG, "Retrieve ${itemFirestore.toObject<Item>()}")
                            CoroutineScope(Dispatchers.IO).launch {
                                itemFirestore.toObject<Item>()?.let { it1 ->
                                    machineDao.update(it1)
                                    Log.d(TAG, "Update ${itemFirestore.toObject<Item>()}")
                                }
                            }
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        compareLastNewItem()
                    }
                }.addOnFailureListener {

                }
        } catch (e: Exception) {

        }

    }

    fun newRandomInsideNumber(product: String): String {
        val random = Random.nextInt(399, 688)
        return "${product.substring(0, 2)}$random"
    }

    private fun postItem(item: Item) = CoroutineScope(Dispatchers.IO).launch {
        try {
            firestoreMachinesDbRef.document(item.id.toString()).set(item).addOnSuccessListener {
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
            firestoreMachinesDbRef.document(item.id.toString())
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
                firestoreMachinesDbRef.document(id.toString()).get().addOnSuccessListener {
                    val item = it.toObject<Item>()
                }.addOnFailureListener {
                    Log.d(TAG, "Retrieve Item $id in Firestore -> $it")
                }
        } catch (e: Exception) {

        }
    }

    fun subscribeToRealtimeUpdates() {
        firestoreMachinesDbRef.addSnapshotListener { querySnapshot, firestoreException ->
            firestoreException?.let {
                Log.d(TAG, it.message.toString())
            }
            querySnapshot?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    compareLastEdit()
                }
            }
        }
    }

    /*
    User data
     */
    private suspend fun localUserVersionIsUpdate(machineStockUser: MachineStockUser): Boolean {
        return if (machineDao.getUserVersion(machineStockUser.uid).first() != null) {
            machineDao.getUserVersion(machineStockUser.uid).first()!! >= machineStockUser.userVersion!!
        } else {
            false
        }
    }

    fun createNewUser(machineStockUser: MachineStockUser) = CoroutineScope(Dispatchers.IO).launch {

        if (!localUserVersionIsUpdate(machineStockUser)) {

            machineDao.insertNewUser(machineStockUser)

            try {
                firestoreUsersDbRef.document(machineStockUser.uid)
                    .set(machineStockUser)
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "${machineStockUser.uid} New User -> ${machineStockUser.name} Succes"
                        )
                    }.addOnFailureListener { e ->
                        Log.d(TAG, "NewStatus in Firestore -> $e")
                    }
            } catch (e: Exception) {
                Log.d(TAG, "NewUser in Firestore -> $e")
            }
        } else {
            Log.d(TAG, "Same User version, dont need Update")
        }

    }

    /*
    Main Menu Preferences
     */
    private suspend fun createMainMenuPreference() {
        MainMenuPreferencesSource.mainMenuPrefencesList.forEach {
            machineDao.insertMainMenuPreferences(it)
        }
        Log.d(TAG, "MainMenuPreferencesDB created")
    }

    suspend fun updateMainMenuPreference(newMainMenuPreferences: MainMenuPreferences) {
        machineDao.updateMainMenuPreferences(newMainMenuPreferences)
    }

    /*
    fun updateUserInfo(listKey: String, uid: String, itemId: Long) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreUsersDbRef.document(uid).get().addOnSuccessListener {
                    val machineStockUser = it.toObject<MachineStockUser>()
                    var userMap = mutableMapOf<String, String>()
                    when (listKey) {
                        "lastVisit" -> {
                            userMap = mutableMapOf(
                                (listKey to ItemIdListManager.addIdToList(
                                    itemId.toString(),
                                    machineStockUser?.lastVisit.toString()
                                ))
                            )
                        }
                    }
                    if (userMap.isNotEmpty()) {
                        firestoreUsersDbRef.document(uid)
                            .update(userMap as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d(TAG, "$uid Update User -> ${userMap.values} Succes")
                            }.addOnFailureListener { e ->
                                Log.d(TAG, "Update in Firestore -> $e")
                            }
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "Retrieve User in Firestore -> ")
                }
            } catch (e: Exception) {
                Log.d(TAG, "NewUser in Firestore -> $e")
            }

        }
    */

}




