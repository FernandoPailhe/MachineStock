package com.ferpa.machinestock.businesslogic

data class DetailUseCases(
    val updateItemUseCase: UpdateItemUseCase,
    val updateStatusUseCase: UpdateStatusUseCase,
    val getItemUseCase: GetItemUseCase,
    val getEditItemEntryUseCase: GetEditItemEntryUseCase
)
