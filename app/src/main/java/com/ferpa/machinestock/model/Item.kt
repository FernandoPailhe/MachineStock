package com.ferpa.machinestock.model

import android.annotation.SuppressLint
import android.telephony.PhoneNumberUtils.formatNumber
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.ferpa.machinestock.R.*
import com.ferpa.machinestock.utilities.Const
import com.ferpa.machinestock.utilities.PhotoListManager
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
    @ColumnInfo(name = "editUser") var editUser: String? = null,
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

fun Item.getFormattedPrice(withPref: Boolean): String {

    return if (price == null) {
        "A definir"
    } else {
        var priceStr = NumberFormat.getIntegerInstance().format(price)
        if (withPref) {
            priceStr = if (currency.equals("USD")) {
                "$priceStr USD"
            } else {
                "$ $priceStr"
            }
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

fun Item.getLocation(): String? {

    return when (location) {
        "Zoi" -> Const.LOCATION_1
        "Can" -> Const.LOCATION_2
        else -> "A definir"
    }
}

fun Item.getInsideNumber(): String? {

    return if (insideNumber != null){
        "Código: $insideNumber"
    } else {
        null
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

fun Item.getStatus(): String {
    return if (status == null || status == ""){
        "NO INFORMADO"
    } else {
        status.uppercase(Locale.getDefault())
    }
}

fun Item.getMachinePhotoList(): List<MachinePhoto> {
    return PhotoListManager.getMachinePhotoList(this)
}

fun Item.addNewPhoto(): String {
    return PhotoListManager.getNewPhotoUrl(this)
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






