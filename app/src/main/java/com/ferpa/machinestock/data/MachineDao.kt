package com.ferpa.machinestock.data

import androidx.room.*
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MachineStockUser
import com.ferpa.machinestock.model.MainMenuPreferences
import kotlinx.coroutines.flow.Flow


@Dao
interface MachineDao {

    /*
    User
     */

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewUser(machineStockUser: MachineStockUser)

    @Update
    suspend fun updateUser(machineStockUser: MachineStockUser)

    @Query ("SELECT userVersion from MachineStockUser WHERE uid = :uid")
    fun getUserVersion(uid: String): Flow<Int?>

    /*
    User Preferences
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMainMenuPreferences(mainMenuPreferences: MainMenuPreferences)

    @Update
    suspend fun updateMainMenuPreferences(mainMenuPreferences: MainMenuPreferences)

    @Query ("SELECT * from MainMenuPreferences")
    fun getMainMenuPreferencesList(): Flow<List<MainMenuPreferences>>

    @Query ("SELECT * from MainMenuPreferences WHERE name = :name")
    fun getMainMenuPreferences(name: String): Flow<MainMenuPreferences>

    @Query ("SELECT priority from MainMenuPreferences WHERE name = :name")
    fun getMainMenuPreferencesExist(name: String): Flow<Int?>

    /*
    Machines
     */
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update //(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Item)

    @Delete
    suspend fun delete (item: Item)

    @Query ("SELECT editDate from item ORDER BY editDate DESC LIMIT 1")
    fun getLastUpdate(): Flow<String>

    @Query ("SELECT insertDate from item ORDER BY insertDate DESC LIMIT 1")
    fun getLastNewMachine(): Flow<String>

    @Query ("SELECT * from item WHERE ID = :id")
    fun getMachine(id: Long): Flow<Item>

    @Query("SELECT DISTINCT product FROM item")
    fun getProductList(): Flow<List<String>>

    /*
    All machines queries
     */
    @Query("SELECT * FROM item ORDER BY editDate DESC")
    fun getAll(): Flow<List<Item>>

    /*
    All machines queries with search
    */
    @Query("SELECT * FROM item WHERE (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery OR product LIKE :searchQuery)")
    fun getSearchQueryAllItems(searchQuery: String): Flow<List<Item>>
    
    @Query("SELECT * FROM item WHERE product = :product AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery)")
    fun getSearchQueryFilterByProduct(product: String, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "AND NOT status IN (:notStatus) " +
            "ORDER BY feature1 ASC")
    fun getProducts(product: String, notStatus: List<String>): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE NOT product IN (:notProduct) " +
            "ORDER BY editDate ASC LIMIT :limit")
    fun getOthersProductsWithLimit(notProduct: List<String>, limit: String): Flow<List<Item>>

}