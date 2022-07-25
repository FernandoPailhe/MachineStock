package com.ferpa.machinestock.model

data class CompanyGroup(
    val id: Long,
    val name: String,
    val companies: List<Company>
)
