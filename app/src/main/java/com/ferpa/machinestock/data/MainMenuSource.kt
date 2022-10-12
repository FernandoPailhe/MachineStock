package com.ferpa.machinestock.data

import android.icu.text.Normalizer.NO
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.utilities.MenuListUtil

object MainMenuSource {

    var mainMenu: List<MainMenuItem> = listOf(
        MainMenuItem(
            "Nuevos ingresos",
            null,
            5,
            false,
            MenuListUtil(
                sortBy = "insertDate",
                listSize = 5,
            ),
            null,
            false
        ),
        MainMenuItem(
            "Novedades",
            null,
            6,
            false,
            MenuListUtil(
                sortBy = "editDate",
                listSize = 5
            ),
            null,
            false
        ),
        MainMenuItem(
            "Vendidas",
            null,
            9,
            false,
            MenuListUtil(
                sortBy = "editDate",
                listSize = 5,
                filterByStatus = listOf("VENDIDA")
            ),
            null
        ),
        MainMenuItem(
            "Señadas",
            null,
            8,
            false,
            MenuListUtil(
                sortBy = "editDate",
                listSize = 5,
                filterByStatus = listOf("SEÑADA")
            ),
            null
        ),
        MainMenuItem(
            "Retiradas",
            null,
            10,
            false,
            MenuListUtil(
                sortBy = "editDate",
                listSize = 5,
                filterByStatus = listOf("RETIRADA")
            ),
            null
        ),
        MainMenuItem(
            "Guillotinas",
            null,
            1,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("GUILLOTINA"),
                filterByStatus = listOf("A REPARAR","DISPONIBLE","RESERVADA","NO INFORMADO")
            ),
            null
        ),
        MainMenuItem(
            "Plegadoras",
            null,
            2,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("PLEGADORA"),
                filterByStatus = listOf("A REPARAR","DISPONIBLE","RESERVADA","NO INFORMADO")
            ),
            null
        ),
        MainMenuItem(
            "Balancines",
            null,
            3,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("BALANCIN"),
                filterByStatus = listOf("A REPARAR","DISPONIBLE","RESERVADA","NO INFORMADO")
            ),
            null
        ),
        MainMenuItem(
            "Tornos",
            null,
            4,
            false,
            MenuListUtil(
                sortBy = "feature1",
                filterByProduct = listOf("TORNO"),
                filterByStatus = listOf("A REPARAR","DISPONIBLE","RESERVADA","NO INFORMADO")
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
                filterByStatus = listOf("A REPARAR","DISPONIBLE","RESERVADA","NO INFORMADO")
            ),
            null,
            false
        ),
        MainMenuItem(
            "Estado no informado",
            null,
            9,
            false,
            MenuListUtil(
                sortBy = "product",
                filterByStatus = listOf("NO INFORMADO")
            ),
            null,
            false
        )
    )

}