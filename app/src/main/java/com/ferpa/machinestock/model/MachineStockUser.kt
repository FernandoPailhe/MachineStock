package com.ferpa.machinestock.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ferpa.machinestock.utilities.ItemIdListManager.Companion.addIdToList

@Entity
data class MachineStockUser(
    @PrimaryKey(autoGenerate = false)
    val uid: String,
    val name: String?,
    val email: String? = null,
    val profilePhotoUrl: String? = null,
    val phoneNumber: String? = null,
    val position: String? = null,
    val nickName: String? = null,
    val license: Int = 0,
    val companyId: String? = null,
    val companyGroupId: String? = null,
    val favorites: String? = null,
    val lastVisits: String? = null,
    val shareWithPrice: Boolean? = false,
    val userVersion: Int? = 1
)

fun MachineStockUser.addToListKey(listKey: String, itemId: Long, oldList: String? = null): Map<String, String> {

    return mapOf(
        (listKey to addIdToList(itemId.toString(), oldList))
    )
}
