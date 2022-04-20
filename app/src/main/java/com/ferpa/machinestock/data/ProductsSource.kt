package com.ferpa.machinestock.data

import com.ferpa.machinestock.R
import com.ferpa.machinestock.model.Product

object ProductsSource {

    val products: List<Product> = listOf(
        Product("TODAS", R.drawable.plegadora),
        Product("GUILLOTINA", R.drawable.maquina),
        Product("PLEGADORA", R.drawable.plegadora),
        Product("BALANCIN", R.drawable.maquina),
        Product("TORNO", R.drawable.torno),
        Product("CILINDRO", R.drawable.plegadora),
        Product("COMPRESOR", R.drawable.plegadora),
        Product("FRESADORA", R.drawable.plegadora),
        Product("PLASMA", R.drawable.plegadora),
        Product("SERRUCHO", R.drawable.plegadora),
        Product("SOLDADURA", R.drawable.plegadora)
    )
}