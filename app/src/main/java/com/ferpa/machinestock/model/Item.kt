package com.ferpa.machinestock.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.ferpa.machinestock.R
import com.ferpa.machinestock.R.*
import com.ferpa.machinestock.utilities.Const
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 74,
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
    @ColumnInfo(name = "editDate") val editDate: String? = getCurrentDate(),
    @ColumnInfo(name = "editUser") val editUser: String? = null,
    @ColumnInfo(name = "excelText") val excelText: String? = null,
    @ColumnInfo(name = "photo1") val photo1: String? = null,
    @ColumnInfo(name = "photo2") val photo2: String? = null,
    @ColumnInfo(name = "photo3") val photo3: String? = null,
    @ColumnInfo(name = "photo4") val photo4: String? = null
)

fun Item.getName(): String? {

    var itemName = brand

    if (brand != null ){
        when (type){
            "H" -> itemName = "$brand - Hidráulica"
            "N" -> itemName = "$brand - Neumática"
            "M" -> itemName = "$brand - Mecánica"
        }
    } else {
        itemName = excelText.toString()
    }
    return itemName
}

fun Item.getType(): String{
    return when (type){
        "H" -> "Hidráulica"
        "N" -> "Neumática"
        "M" -> "Mecánica"
        else -> "NO ESPECÍFICA"
    }
}

fun Item.getFormattedPrice(): String {

    var priceInt = NumberFormat.getIntegerInstance().format(price)

    var priceStr = "$priceInt"

    when (currency) {
        "USD" -> priceStr = "$priceInt USD"
    }

    return priceStr
}

fun Item.getFeatures(): String {

    var features = when (product) {
        "GUILLOTINA" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} mm"
        "PLEGADORA" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} tns"
        "BALANCIN" -> "${formatNumber(feature1)} tns"
        "TORNO" -> "${formatNumber(feature1)} mm x ${formatNumber(feature2)} mm"
        "COMPRESOR" -> "${formatNumber(feature1)} hp / ${formatNumber(feature2)} Volts"
        "LIMADORA" -> "${formatNumber(feature1)} mm"
        "SERRUCHO" -> "${formatNumber(feature1)} pulgadas"
        "SOLDADURA" -> "${formatNumber(feature1)} mig"
        else -> excelText.toString()
    }

    if (features == "null"){ features = ""}

    return features
}

fun Item.getLocation(): String {

    return when (location) {
        "Zoi" -> Const.LOCATION_1
        "Zoilo" -> Const.LOCATION_1
        "Can" -> Const.LOCATION_2
        "Canavese" -> Const.LOCATION_2
        else -> "A definir"
    }
}

fun Item.getColor(): Int{

    var statusItemColor = when (status){
        "A REPARAR" -> drawable.gradient_list_item_status1
        "SEÑADA" -> drawable.gradient_list_item_status3
        "VENDIDA" -> drawable.gradient_list_item_status4
        //"Retirada" -> drawable.gradient_list_item_status5
        else -> drawable.gradient_list_item
    }

    return statusItemColor
}

fun formatNumber (number : Double?): String {

    return if (number != null) {
        if (number.rem(1).equals(0.0)){
            NumberFormat.getIntegerInstance().format(number)
        } else {
            number.toString()
        }
    } else {
        ""
    }

}

fun getCurrentDate():String{
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(Date())
}

fun Item.getMachinePhotoList(): List<MachinePhoto> {

    var machinePhotoList: MutableList<MachinePhoto> = mutableListOf(
        MachinePhoto(1, photo1),
        MachinePhoto(2, photo2),
        MachinePhoto(3, photo3),
        MachinePhoto(4, photo4)
    )

    return machinePhotoList
}





