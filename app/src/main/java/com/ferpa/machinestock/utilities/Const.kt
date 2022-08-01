package com.ferpa.machinestock.utilities

object Const {


    const val LOCATION_1 = "Depósito 1"
    const val LOCATION_2 = "Depósito 2"

    const val OWNER_1 = "Dueño 1"
    const val OWNER_2 = "Dueño 2"

    const val SOCIEDAD = "Sociedad"

    const val MENU_LIST_SIZE = 5

    const val BASE_FIRESTORE = "company_users"
    const val COMPANY_USER = "/canavese"
    const val USED_FIRESTORE_DB = "$BASE_FIRESTORE$COMPANY_USER/used_machines"
    const val USERS_FIRESTORE_DB = "$BASE_FIRESTORE$COMPANY_USER/users"

    const val FIRESTORAGE_BASE_URL = "gs://machinestock.appspot.com/"
    const val USED_MACHINES_PHOTO_BASE_URL = "$BASE_FIRESTORE$COMPANY_USER/usedmachinesphotos"

    const val REQUEST_GALLERY_PHOTO = 199
    const val REQUEST_TAKE_PHOTO = 198


}