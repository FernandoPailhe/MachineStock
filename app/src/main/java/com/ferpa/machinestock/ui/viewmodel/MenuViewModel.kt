package com.ferpa.machinestock.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.ferpa.machinestock.data.MachinesRepository
import com.ferpa.machinestock.data.MainMenuPreferencesSource
import com.ferpa.machinestock.model.MainMenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class MenuViewModel
@Inject
constructor(private val machinesRepository: MachinesRepository) : ViewModel() {

    val mainMenuMachineList: LiveData<List<MainMenuItem>> get() = machinesRepository.menuList.asLiveData()

    private var _currentMainMenuPreferences = MainMenuPreferencesSource.mainMenuPrefencesList

    private val _menuPosition = MutableStateFlow(arrayListOf(0, 0))
    val menuPosition: MutableStateFlow<ArrayList<Int>> get() = _menuPosition

    init {
        compareDatabases()
    }

    /*
     * Network
     */
    private fun compareDatabases() {
        viewModelScope.launch {
            try {
                machinesRepository.subscribeToRealtimeUpdates()
            } catch (e: Exception) {
                Log.e("MenuViewModel", "CompareNetworkItems $e")
            }
        }
    }

    /*
     * Update MainMenuPreferences
     */
    fun setExpand(expand: Boolean, name: String) {
        _currentMainMenuPreferences.find {
            it.name == name
        }?.initiallyExpanded = expand
    }

    suspend fun updateMainMenuPreferences() {
        _currentMainMenuPreferences.forEach {
            Log.d("MenuViewModel", "Update ${it.name}")
            machinesRepository.updateMainMenuPreference(it)
        }
    }

    /*
     * User History and Settings
     */
    fun setMenuFragmentRecyclerViewPosition(mainPosition: Int, childPosition: Int) {
        _menuPosition.value[0] = mainPosition
        _menuPosition.value[1] = childPosition
    }

}