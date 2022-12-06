package com.ferpa.machinestock.businesslogic

import com.ferpa.machinestock.utilities.Extensions.stringToIntOrEmptyToZero
import javax.inject.Inject

class IsEntryValidUseCase @Inject constructor() {

    operator fun invoke(
        product: String,
        itemFeature1: String,
        itemFeature2: String,
        owner1: String?,
        owner2: String?
    ): Int {

        var isValid = 0

        if (!isFeatureEntryValid(product, itemFeature1, itemFeature2)) {
            isValid = 1
        } else if (!isOwnerEntryValid(
                owner1.stringToIntOrEmptyToZero(),
                owner2.stringToIntOrEmptyToZero()
            )
        ) {
            isValid = 2
        }

        return isValid

    }

    /*
     * Validates Entries
     */
    private fun isOwnerEntryValid(owner1: Int?, owner2: Int?): Boolean {
        val total = owner1!! + owner2!!

        return (total <= 100)
    }

    private fun isFeatureEntryValid(
        product: String,
        itemFeature1: String,
        itemFeature2: String
    ): Boolean {
        return when (product) {
            "GUILLOTINA" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "PLEGADORA" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "PESTAÑADORA" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "CILINDRO" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "BALANCIN" -> itemFeature1.isNotBlank()
            "TORNO" -> !(itemFeature1.isBlank() || itemFeature2.isBlank())
            "CEPILLO" -> itemFeature1.isNotBlank()
            "CLARK" -> itemFeature1.isNotBlank()
            "FRESADORA" -> itemFeature1.isNotBlank() || itemFeature2.isNotBlank()
            "COMPRESOR" -> itemFeature1.isNotBlank()
            "LIMADORA" -> itemFeature1.isNotBlank()
            "PLASMA" -> itemFeature1.isNotBlank()
            "PLATO" -> itemFeature1.isNotBlank()
            "RECTIFICADORA" -> itemFeature1.isNotBlank()
            "SERRUCHO" -> itemFeature1.isNotBlank()
            "SOLDADURA" -> itemFeature1.isNotBlank()
            "HIDROCOPIADOR" -> true
            else -> true
        }
    }

}