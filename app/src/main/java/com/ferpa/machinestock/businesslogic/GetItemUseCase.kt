package com.ferpa.machinestock.businesslogic

import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.Item
import javax.inject.Inject

class GetItemUseCase @Inject constructor(private val machinesRepository: MachinesRepository) {

    operator fun invoke(machineId: Long) = machinesRepository.getItem(machineId)

}