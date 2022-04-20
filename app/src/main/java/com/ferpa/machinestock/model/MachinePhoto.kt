package com.ferpa.machinestock.model

import com.squareup.moshi.Json

data class MachinePhoto(
    val id: Int,
    @Json(name = "img_src") val imgSrcUrl: String?
)
