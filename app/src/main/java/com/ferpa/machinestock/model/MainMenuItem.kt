package com.ferpa.machinestock.model

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import com.ferpa.machinestock.utilities.CustomListUtil
import com.ferpa.machinestock.utilities.MenuListUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

data class MainMenuItem(
    val name: String,
    @DrawableRes val imageResourceId: Int?,
    var priority: Int?,
    var hasNews: Boolean = false,
    var menuListUtil: MenuListUtil = MenuListUtil(),
    var itemList: List<Item>?,
    val initiallyExpanded: Boolean = true,
    val visible: Boolean? = true,
)
