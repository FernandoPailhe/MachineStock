package com.ferpa.machinestock.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.model.MachineStockUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor (private val machinesRepository: MachinesRepository): ViewModel(){

    /*
     *  User
     */
    fun createUser(machineStockUser: MachineStockUser) {
        machinesRepository.createNewUser(machineStockUser)
    }

}