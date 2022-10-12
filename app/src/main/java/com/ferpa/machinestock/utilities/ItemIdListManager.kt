package com.ferpa.machinestock.utilities

class ItemIdListManager {

    companion object {

        private const val ID_SEPARATOR = "/"

        fun addIdToList(id: String, oldList: String? = null): String {
            return if (oldList != null) {
                makeNewListToString(id, oldList)
            } else {
                id
            }
        }

        private fun makeNewListToString(id: String, oldList: String, limit: Int? = null): String {
            val newList = mutableListOf(id)
            getIdList(oldList, limit).forEachIndexed { index, oldId ->
                if (oldId != id) {
                    if (limit != null) {
                        if (index < limit) {
                            newList.add(oldList)
                        }
                    } else {
                        newList.add(oldList)
                    }
                }
            }

            return newList.joinToString { ID_SEPARATOR }
        }

        private fun getIdList(idString: String?, limit: Int? = null): List<String> {
            return if (idString != null) {
                val idList = mutableListOf<String>()
                idString.split(ID_SEPARATOR).toList().forEach {
                    idList.add(it)
                }
                if (limit != null && limit < idList.size) {
                    idList.subList(0, limit)
                } else {
                    idList
                }
            } else {
                return emptyList()
            }
        }

    }
}