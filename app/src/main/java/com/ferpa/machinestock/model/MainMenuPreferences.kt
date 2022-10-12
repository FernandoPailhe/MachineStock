package com.ferpa.machinestock.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MainMenuPreferences(
    @PrimaryKey (autoGenerate = false)
    val name: String,
    var initiallyExpanded: Boolean = true,
    val priority: Int? = null,
    val visible: Boolean? = true,
)
