package com.ferpa.machinestock.data

import com.ferpa.machinestock.model.MenuItem
import com.ferpa.machinestock.utilities.MenuListUtil

object MainMenuSource {

    var mainMenu: List<MenuItem> = listOf(
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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
        MenuItem(
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