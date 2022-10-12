package com.ferpa.machinestock.model

data class CompanyGroup(
    val id: String,
    val name: String,
    val companies: List<Company>
)
