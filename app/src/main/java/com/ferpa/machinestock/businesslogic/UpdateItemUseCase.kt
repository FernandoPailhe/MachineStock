package com.ferpa.machinestock.businesslogic

import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.Item
import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(private val machinesRepository: MachinesRepository) {

    suspend operator fun invoke(machine: Item) = machinesRepository.updateItem(machine)

}