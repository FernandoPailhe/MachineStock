package com.ferpa.machinestock.utilities

sealed class DataState<out R> {

    data class Succes <out T>(val data: T): DataState<T>()
    data class Error (val exception: Exception): DataState<Nothing>()
    object Loading: DataState<Nothing>()

}