package com.ferpa.machinestock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.databinding.FragmentMenuBinding
import com.ferpa.machinestock.ui.adapter.MenuAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MenuFragment : Fragment(R.layout.fragment_menu) {


    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.compareDatabases()

        val adapter = MenuAdapter{
            val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment(it.name.toString())
            viewModel.setProduct(it.name.toString())
            this.findNavController().navigate(action)
        }

        binding.apply {
            menuRecyclerView.adapter = adapter
            menuRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
        }

    }


}