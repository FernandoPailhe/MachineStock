package com.ferpa.machinestock.model

import android.net.MailTo
import android.provider.ContactsContract

data class User(
    val uid: String,
    val name: String,
    val sourName: String?,
    val position: String?,
    val profilePhotoUrl: String?,
    val nickname: String?,
    val email: String,
    val license: Int = 0,
    val company: Company?,
    val companyGroup: CompanyGroup?,
    val favorites: MutableList<Long>,
    val lastVisit: MutableList<Long>
)
