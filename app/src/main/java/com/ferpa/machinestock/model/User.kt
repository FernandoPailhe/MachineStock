package com.ferpa.machinestock.model

import android.net.MailTo
import android.provider.ContactsContract

data class User(
    val id: String,
    val name: String,
    val sourName: String?,
    val position: String?,
    val profilePhotoUrl: String?,
    val nickname: String?,
    val username: String?,
    val mailTo: MailTo?,
    val license: Long?,
    val company: Company?,
    val companyGroup: CompanyGroup?
)
