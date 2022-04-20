package com.ferpa.machinestock.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.MachineStockApplication
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentMenuBinding
import com.ferpa.machinestock.ui.adapter.MenuAdapter



class MenuFragment : Fragment() {


    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!


    private val viewModel: MachineStockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val adapter = MenuAdapter{
            val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment(it.name.toString())
            this.findNavController().navigate(action)
        }

        binding.apply {
            menuRecyclerView.adapter = adapter
            menuRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
        }

    }


}