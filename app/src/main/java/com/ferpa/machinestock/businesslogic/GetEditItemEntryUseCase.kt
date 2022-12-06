package com.ferpa.machinestock.businesslogic

import com.ferpa.machinestock.model.Item
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class GetEditItemEntryUseCase @Inject constructor() {

    operator fun invoke(
        item: Item,
        product: String,
        type: String?,
        feature1: String,
        feature2: String,
        feature3: String,
        price: Double,
        brand: String?,
        insideNumber: String?,
        location: String?,
        currency: String?,
        status: String?,
        owner2: Int?,
        owner1: Int?,
        observations: String?
    ): Item {
        return Item(
            id = item.id,
            insertDate = item.insertDate,
            product = product.uppercase(),
            insideNumber = insideNumber,
            location = location,
            brand = brand,
            feature1 = feature1.toDoubleOrNull(),
            feature2 = feature2.toDoubleOrNull(),
            feature3 = feature3,
            price = price,
            owner1 = owner1,
            owner2 = owner2,
            currency = currency,
            type = type,
            status = status?.uppercase(),
            observations = observations,
            editUser = Firebase.auth.currentUser?.displayName.toString(),
            photos = item.photos
        )
    } //Build edit item Object

}