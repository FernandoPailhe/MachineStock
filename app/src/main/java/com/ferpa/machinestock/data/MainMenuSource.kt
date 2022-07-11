package com.ferpa.machinestock.data

import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.utilities.MenuListUtil

object MainMenuSource {

    var mainMenu: List<MainMenuItem> = listOf(
        MainMenuItem(
            "Nuevos ingresos",
            null,
            1,
            false,
            MenuListUtil(
                sortBy = "insertDate",
                listSize = 5,
            ),
            null
        ),
        MainMenuItem(
            "Novedades",
            null,
            2,
            false,
            MenuListUtil(
                sortBy = "editDate",
                listSize = 5
            ),
            null
        ),
        MainMenuItem(
            "Vendidas",
            null,
            8,
            false,
            MenuListUtil(
                sortBy = "feature1",
                listSize = 5,
                filterByStatus = listOf("Vendida","Retirada")
            ),
            null
        ),
        MainMenuItem(
            "Guillotinas",
            null,
            4,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("GUILLOTINA"),
                filterByStatus = listOf("A reparar","Disponible","Reservada","Señada","No informado")
            ),
            null
        ),
        MainMenuItem(
            "Plegadoras",
            null,
            5,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("PLEGADORA"),
                filterByStatus = listOf("NOT","Vendida","Retirada")
            ),
            null
        ),
        MainMenuItem(
            "Balancines",
            null,
            6,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("BALANCIN"),
                filterByStatus = listOf("NOT","Vendida","Retirada")
            ),
            null
        ),
        MainMenuItem(
            "Tornos",
            null,
            3,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("TORNO"),
                filterByStatus = listOf("NOT","Vendida","Retirada")
            ),
            null
        ),
        MainMenuItem(
            "Otros",
            null,
            7,
            false,
            MenuListUtil(
                sortBy = "product",
                filterByProduct = listOf("NOT","GUILLOTINA","PLEGADORA","BALANCIN","TORNO"),
                filterByStatus = listOf("NOT","Vendida","Retirada")
            ),
            null
        )
    )

}