package com.ferpa.machinestock.data

import android.icu.text.UnicodeSet.CASE
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.ferpa.machinestock.model.Item

@Dao
interface ItemDao {

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update //(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Item)

    @Delete
    suspend fun delete (item: Item)

    @Query ("SELECT * from item WHERE ID = :id")
    fun getItem(id: Long): Flow<Item>

    @Query("SELECT DISTINCT product FROM item")
    fun getProductList(): Flow<List<String>>

    /*
    Separate product Queries
     */
    @Query("SELECT * FROM item " +
            "WHERE product IN (:product) " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getFilteredProduct(product: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product IN (:product) AND status IN (:statusFilterList) " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getFilteredProductAndStatus(product: List<String>, statusFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product IN (:product) AND type IN (:typeFilterList) " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getFilteredProductAndType(product: List<String>, typeFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product NOT IN (:product) AND status IN (:statusFilterList) " +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getFilteredNotProductAndStatus(product: List<String>, statusFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product IN (:product) AND status IN (:statusFilterList) AND type IN (:typeFilterList) " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getFilteredProductStatusAndType(product: List<String>, statusFilterList: List<String>, typeFilterList: List<String>, limit: String): Flow<List<Item>>

    /*
    Separate product Queries with search
     */
    @Query("SELECT * FROM item " +
            "WHERE product = :product AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductWithSearch(product: String, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND status IN (:statusFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductAndStatusWithSearch(product: String, statusFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND type IN (:typeFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductAndTypeWithSearch(product: String, typeFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND status IN (:statusFilterList) AND type IN (:typeFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductStatusAndTypeWithSearch(product: String, statusFilterList: List<String>, typeFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    /*
    All machines queries
     */
    @Query("SELECT * FROM item ORDER BY editDate DESC")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM item ORDER BY editDate DESC LIMIT :limit")
    fun getAllWithLimit(limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item ORDER BY insertDate DESC LIMIT :limit")
    fun getAllWithLimitSortByInsertDate(limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE owner1 IN (:ownerList) " +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterOwner(ownerList: List<Int>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) " +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterStatus(statusFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) AND owner1 IN (:ownerList)" +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterStatusAndOwner(statusFilterList: List<String>, ownerList: List<Int>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE type IN (:typeFilterList) " +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterType(typeFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE type IN (:typeFilterList) AND owner1 IN (:ownerList)" +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterTypeAndOwner(typeFilterList: List<String>, ownerList: List<Int>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) AND type IN (:typeFilterList) " +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterStatusAndType(statusFilterList: List<String>, typeFilterList: List<String>, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) AND type IN (:typeFilterList) AND owner1 IN (:ownerList)" +
            "ORDER BY editDate DESC LIMIT :limit")
    fun getAllFilterStatusOwnerAndType(statusFilterList: List<String>, ownerList: List<Int>, typeFilterList: List<String>, limit: String): Flow<List<Item>>

    /*
    All machines queries with search
    */
    @Query("SELECT * FROM item WHERE (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery OR product LIKE :searchQuery)")
    fun getSearchQueryAllItems(searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getAllFilterStatusWithSearch(statusFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE type IN (:typeFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getAllFilterTypeWithSearch(typeFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE status IN (:statusFilterList) AND type IN (:typeFilterList) AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery) " +
            "ORDER BY feature1 ASC")
    fun getAllFilterStatusAndTypeWithSearch( statusFilterList: List<String>, typeFilterList: List<String>, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "ORDER BY feature1 ASC")
    fun getProducts(product: String): Flow<List<Item>>

    /*
    Main Menu Queries
     */
    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getProductsWithLimit(product: String, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE NOT product in (:notProduct)" +
            "ORDER BY editDate ASC LIMIT :limit")
    fun getOthersProductsWithLimit(notProduct: List<String>, limit: String): Flow<List<Item>>

}