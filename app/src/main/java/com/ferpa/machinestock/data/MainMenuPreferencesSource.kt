package com.ferpa.machinestock.data

import android.icu.text.Normalizer.NO
import com.ferpa.machinestock.model.MainMenuItem
import com.ferpa.machinestock.model.MainMenuPreferences
import com.ferpa.machinestock.utilities.MenuListUtil

object MainMenuPreferencesSource {

    var mainMenuPrefencesList: List<MainMenuPreferences> = listOf(
        MainMenuPreferences(
            "Nuevos ingresos",
            true,
            2
        ),
        MainMenuPreferences(
            "Novedades",
            true,
            3
        ),
        MainMenuPreferences(
            "Señadas",
            false,
            9
        ),
        MainMenuPreferences(
            "Vendidas",
            false,
            11
        ),
        MainMenuPreferences(
            "Retiradas",
            false,
            12
        ),
        MainMenuPreferences(
            "Guillotinas",
            false,
            4
        ),
        MainMenuPreferences(
            "Plegadoras",
            true,
            5
        ),
        MainMenuPreferences(
            "Balancines",
            true,
            6
        ),
        MainMenuPreferences(
            "Tornos",
            true,
            7
        ),
        MainMenuPreferences(
            "Otros",
            false,
            8
        ),
        MainMenuPreferences(
            "Estado no informado",
            false,
            13,
            visible = false
        ),
        MainMenuPreferences(
            "Favoritos",
            true,
            1
        ),
        MainMenuPreferences(
            "Ultimas visitadas",
            false,
            10
        )

    )

}