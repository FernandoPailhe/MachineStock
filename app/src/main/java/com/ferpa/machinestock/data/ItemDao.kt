package com.ferpa.machinestock.data

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
            "WHERE product = :product ORDER BY feature1 ASC")
    fun getProducts(product: String): Flow<List<Item>>

    @Query("SELECT * FROM item ORDER BY editDate")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE product = :product AND (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery)")
    fun getSearchQuery(product: String, searchQuery: String): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE (brand LIKE :searchQuery OR insideNumber LIKE :searchQuery OR feature1 LIKE :searchQuery OR feature2 LIKE :searchQuery OR feature3 LIKE :searchQuery OR product LIKE :searchQuery)")
    fun getSearchQueryAll(searchQuery: String): Flow<List<Item>>

}