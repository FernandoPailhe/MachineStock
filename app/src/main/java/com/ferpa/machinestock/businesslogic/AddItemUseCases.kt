package com.ferpa.machinestock.businesslogic

data class AddItemUseCases(
    val updateItemUseCase: UpdateItemUseCase,
    val insertItemUseCase: InsertItemUseCase,
    val getItemUseCase: GetItemUseCase,
    val getEditItemEntryUseCase: GetEditItemEntryUseCase,
    val getNewItemEntryUseCase: GetNewItemEntryUseCase,
    val isEntryValidUseCase: IsEntryValidUseCase
)
