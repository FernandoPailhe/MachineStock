package com.ferpa.machinestock.utilities

object Const {

    const val LOCATION_1 = "Depósito 1"
    const val LOCATION_2 = "Depósito 2"
    const val LOCATION_3 = "Depósito 3"
    const val LOCATION_4 = "Depósito 4"

    const val OWNER_1 = "Socio A"
    const val OWNER_2 = "Socio B"

    const val SOCIEDAD = "Sociedad"

    const val MENU_LIST_SIZE = 50
    const val PREFS_VERSION = 1

    private const val BASE_FIRESTORE = "company_users"
    private const val COMPANY_USER = "/example_user"
    const val MACHINES_FIRESTORE_DB = "$BASE_FIRESTORE$COMPANY_USER/machines"
    const val USERS_FIRESTORE_DB = "$BASE_FIRESTORE$COMPANY_USER/users"

    const val FIRESTORAGE_BASE_URL = "gs://machinestock.appspot.com/"
    const val USED_MACHINES_PHOTO_BASE_URL = "$BASE_FIRESTORE/canavese/usedmachinesphotos"

    const val REQUEST_CODE_GOOGLE_LOG_IN = 170
    const val REQUEST_CODE_GALLERY_PHOTO = 199
    const val REQUEST_CODE_TAKE_PHOTO = 198

    const val NEW_ITEM: Long = 0

}