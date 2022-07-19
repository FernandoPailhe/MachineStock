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

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "ORDER BY feature1 ASC")
    fun getFilteredProduct(product: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND status IN (:statusFilterList) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductAndStatus(product: String, statusFilterList: List<String>): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND type IN (:typeFilterList) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductAndType(product: String, typeFilterList: List<String>): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product AND status IN (:statusFilterList) AND type IN (:typeFilterList) " +
            "ORDER BY feature1 ASC")
    fun getFilteredProductStatusAndType(product: String, statusFilterList: List<String>, typeFilterList: List<String>): Flow<List<Item>>

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

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "ORDER BY feature1 ASC")
    fun getProducts(product: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "AND NOT status = :notStatus1 OR status = :notStatus2 " +
            "ORDER BY feature1 ASC")
    fun getOnlyAvailableProducts(product: String, notStatus1: String, notStatus2: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE product = :product " +
            "ORDER BY feature1 ASC LIMIT :limit")
    fun getProductsWithLimit(product: String, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item " +
            "WHERE NOT product = :product1 OR product = :product2 OR product = :product3 OR product = :product4 " +
            "ORDER BY editDate ASC LIMIT :limit")
    fun getOthersProductsWithLimit(product1: String, product2: String, product3:String, product4: String, limit: String): Flow<List<Item>>

    @Query("SELECT * FROM item ORDER BY editDate")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE product = :product AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery)")
    fun getSearchQueryFilterByProduct(product: String, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery OR product LIKE :searchQuery)")
    fun getSearchQueryAllItems(searchQuery: String): Flow<List<Item>>

}