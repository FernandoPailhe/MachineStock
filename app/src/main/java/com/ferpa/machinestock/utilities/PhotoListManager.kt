package com.ferpa.machinestock.utilities

import android.util.Log
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.MachinePhoto
import com.ferpa.machinestock.model.getTimeStamp
import com.ferpa.machinestock.utilities.Const.USED_MACHINES_PHOTO_BASE_URL

class PhotoListManager {

    companion object {

        private const val PHOTO_SEPARATOR = "/"
        private const val URL_NAME_SEPARATOR = "_"
        private const val THUMBNAIL = "t"
        private const val TAG = "PhotoListManager"

        fun getNewPhotoUrl(item: Item): String {
            return if (item.photos != "0") {
                val newPhoto = newPhotoId(getPhotoList(item))
                "${USED_MACHINES_PHOTO_BASE_URL}/${item.id}${URL_NAME_SEPARATOR}${newPhoto}"
            } else {
                "${USED_MACHINES_PHOTO_BASE_URL}/${item.id}${URL_NAME_SEPARATOR}1"
            }
        }

        fun getDeleteUrl(item: Item, index: Int): String =
            "${USED_MACHINES_PHOTO_BASE_URL}/${item.id}${URL_NAME_SEPARATOR}${getPhotoList(item)[index]}"

        fun getMachinePhotoList(item: Item, isThumbnail: Boolean = false): List<MachinePhoto> {
            var thumbnailChar = ""
            if (isThumbnail) {
                thumbnailChar = THUMBNAIL
            }
            return if (item.photos != "0") {
                val mutableList: MutableList<Int> = getPhotoList(item).toMutableList()
                mutableList.map {
                    MachinePhoto(
                        it,
                        "${USED_MACHINES_PHOTO_BASE_URL}/${item.id}${URL_NAME_SEPARATOR}${it}${thumbnailChar}"
                    )
                }
            } else {
                mutableListOf(MachinePhoto(-1, item.product))
            }
        }

        fun addNewPhoto(item: Item): Item {
            return updatePhotoList(
                item,
                stringFromPhotoList(
                    addPhotoToList(
                        getPhotoList(item)
                    )
                )
            )
        }

        fun deletePhoto(item: Item, deletePhotoIndex: Int): Item {
            return updatePhotoList(
                item,
                stringFromPhotoList(
                    removePhotoFromList(
                        getPhotoList(item),
                        deletePhotoIndex
                    )
                )
            )

        }

        private fun newPhotoId(list: List<Int>): Int =
            if (list.isEmpty()) 1 else list.maxOrNull()!!.plus(1)

        private fun addPhotoToList(list: List<Int>): List<Int> {

            return if (list.isEmpty()) {
                listOf<Int>(1)
            } else {
                val tempList: MutableList<Int> = list.toMutableList()
                Log.d(TAG, "PhotoList before add -> $tempList")
                val newPhotoId = list.maxOrNull()!!.plus(1)
                tempList.add(newPhotoId)
                Log.d(TAG, "PhotoList after add -> $tempList")
                tempList.toList()
            }

        }

        private fun removePhotoFromList(list: List<Int>, index: Int): List<Int> {
            return if (list.size > index) {
                val tempList: MutableList<Int> = list.toMutableList()
                Log.d(TAG, "PhotoList before delete -> $tempList")
                tempList.removeAt(index)
                Log.d(TAG, "PhotoList after delete -> $tempList")
                tempList.toList()
            } else {
                list
            }
        }

        private fun stringFromPhotoList(intPhotoList: List<Int>): String {
            return if (intPhotoList.isEmpty()) {
                "0"
            } else {
                intPhotoList.joinToString(separator = PHOTO_SEPARATOR)
            }
        }

        private fun getPhotoList(item: Item): List<Int> {
            return if (item.photos != "0") {
                val photoList = mutableListOf<Int>()
                item.photos?.split("/")?.toList()?.forEach {
                    photoList.add(
                        it.toInt()
                    )
                }
                photoList
            } else {
                return emptyList()
            }
        }

        private fun updatePhotoList(item: Item, newList: String): Item {
            item.editDate = getTimeStamp()
            item.photos = newList

            return item
        }

    }

}