package com.ferpa.machinestock.model


import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.ferpa.machinestock.R.*
import com.ferpa.machinestock.utilities.Const
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = newProductId(),
    @ColumnInfo(name = "insertDate") val insertDate: String? = getCurrentDate(),
    @ColumnInfo(name = "product") val product: String,
    @ColumnInfo(name = "owner1") val owner1: Int? = 0,
    @ColumnInfo(name = "owner2") val owner2: Int? = 0,
    @ColumnInfo(name = "insideNumber") val insideNumber: String? = "",
    @ColumnInfo(name = "location") val location: String? = "",
    @ColumnInfo(name = "brand") val brand: String? = "",
    @ColumnInfo(name = "feature1") val feature1: Double? = 0.0,
    @ColumnInfo(name = "feature2") val feature2: Double? = 0.0,
    @ColumnInfo(name = "feature3") val feature3: String? = null,
    @ColumnInfo(name = "price") val price: Double? = 0.0,
    @ColumnInfo(name = "currency") val currency: String? = "$",
    @ColumnInfo(name = "type") val type: String? = "",
    @ColumnInfo(name = "status") val status: String? = "no informado",
    @ColumnInfo(name = "observations") val observations: String? = null,
    @ColumnInfo(name = "editDate") var editDate: String? = getCurrentDate(),
    @ColumnInfo(name = "editUser") val editUser: String? = null,
    @ColumnInfo(name = "excelText") val excelText: String? = null,
    @ColumnInfo(name = "photos") var photos: String? = "0"
)

fun Item.getName(): String? {

    var itemName = brand

    if (brand != null) {
        when (type) {
            "H" -> itemName = "$brand - Hidráulica"
            "N" -> itemName = "$brand - Neumática"
            "M" -> itemName = "$brand - Mecánica"
        }
    } else {
        itemName = excelText.toString()
    }
    return itemName
}

fun Item.getType(): String {
    return when (type) {
        "H" -> "Hidráulica"
        "N" -> "Neumática"
        "M" -> "Mecánica"
        else -> "NO ESPECÍFICA"
    }
}

fun Item.getFormattedPrice(): String {

    return if (price == null) {
        "A definir"
    } else {
        var priceStr = NumberFormat.getIntegerInstance().format(price)
        when (currency) {
            "USD" -> priceStr = "$priceStr USD"
        }
        priceStr
    }
}

fun Item.getFeatures(): String {

    var features = when (product) {
        "GUILLOTINA" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} mm"
        "PLEGADORA" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} tns"
        "BALANCIN" -> "${formatNumber(feature1)} tns"
        "TORNO" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} mm"
        "COMPRESOR" -> "${formatNumber(feature1)} hp / ${formatNumber(feature2)} Volts"
        "CEPILLO" -> "${formatNumber(feature1)} tns"
        "CLARK" -> "${formatNumber(feature1)} mm"
        "FRESADORA" -> "${formatNumber(feature1)} mm"
        "LIMADORA" -> "${formatNumber(feature1)} mm"
        "PLASMA" -> "${formatNumber(feature1)} amp"
        "PLATO" -> "${formatNumber(feature1)} mm"
        "RECTIFICADORA" -> "${formatNumber(feature1)} mm"
        "SERRUCHO" -> "${formatNumber(feature1)} pulgadas"
        "SOLDADURA" -> "${formatNumber(feature1)} mig"
        "HIDROCOPIADOR" -> ""
        else -> excelText.toString()
    }

    if (features == "null" || feature1 == null) {
        features = ""
    }

    return features
}

fun Item.getLocation(): String {

    return when (location) {
        "Zoi" -> Const.LOCATION_1
        "Can" -> Const.LOCATION_2
        else -> "A definir"
    }
}

fun Item.getBackgroundColor(): Int {

    val statusItemColor = when (status) {
        "A REPARAR" -> drawable.gradient_list_item_status1
        "SEÑADA" -> drawable.gradient_list_item_status3
        "RESERVADA" -> drawable.gradient_list_item_status3
        "VENDIDA" -> drawable.gradient_list_item_status4
        "RETIRADA" -> drawable.gradient_list_item_status4
        else -> drawable.gradient_list_item
    }

    return statusItemColor
}

fun Item.getMachinePhotoList(): List<MachinePhoto> {

    return if (photos != "0") {
        val photoList = mutableListOf<MachinePhoto>()
        photos?.split("/")?.toList()?.forEachIndexed { index, photoId ->
            photoList.add(
                MachinePhoto(
                    index,
                    "${Const.USED_MACHINES_PHOTO_BASE_URL}/${id}_${photoId}"
                )
            )
        }
        photoList
    } else {
        emptyList()
    }

}

fun Item.addNewPhoto(): String {
    return if (photos != "0") {
        val newPhoto = (photos?.split("/")?.toList()?.last()?.toInt()?.plus(1)).toString()
        "${id}_${newPhoto}"
    } else {
        "${id}_1"
    }
}

//TODO Check this function
fun Item.updatePhotos(newPhoto: String): Item{
    val updateItem = this
    updateItem.editDate = getCurrentDate()
    if (this.photos != "0"){
        updateItem.photos += "/${newPhoto}"
    } else {
        updateItem.photos = "1"
    }

    return updateItem
}

//TODO Check this function
fun Item.deletePhoto(deletePhoto: String): Item{
    val updateItem = this
    updateItem.editDate = getCurrentDate()
    updateItem.photos?.replaceFirst("/${deletePhoto}", "")

    return updateItem

}

fun Item.getObservations(): String{

    return if (observations?.startsWith("SUF_", true) == true || observations?.startsWith("2SUF_", true) == true){
        ""
    } else {
        observations ?: ""
    }

}

fun formatNumber(number: Double?): String {

    return if (number != null) {
        if (number.rem(1).equals(0.0)) {
            NumberFormat.getIntegerInstance().format(number)
        } else {
            number.toString()
        }
    } else {
        ""
    }

}

@SuppressLint("SimpleDateFormat")
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm")
    return sdf.format(Date())
}

@SuppressLint("SimpleDateFormat")
fun newProductId(): Long {
    val sdf = SimpleDateFormat("yyMMddhhmmssSSS")
    return (sdf.format(Date())).toLong()
}






