package com.ferpa.machinestock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentHomeBinding
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.isDbUpdate.collect {
                if (it){
                    val action = HomeFragmentDirections.actionHomeFragmentToMenuFragment()
                    findNavController().navigate(action)
                }
            }
        }

    }

    companion object {
        const val TAG = "HomeFragment"
    }
}