package com.ferpa.machinestock.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.databinding.FragmentMenuBinding
import com.ferpa.machinestock.ui.adapter.ItemListAdapter
import com.ferpa.machinestock.ui.adapter.MenuListAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MenuFragment : Fragment(R.layout.fragment_menu), MenuListAdapter.OnItemClickListener {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    private var idList: MutableList<MutableList<Long>> = mutableListOf()

    private lateinit var menuListAdapter: MenuListAdapter

    private lateinit var searchListAdapter: ItemListAdapter

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

        menuListAdapter = MenuListAdapter(this)


        subscribeUi(menuListAdapter)

        binding.apply {
            menuRecyclerView.adapter = menuListAdapter
            menuRecyclerView.layoutManager = LinearLayoutManager(this@MenuFragment.requireContext())

            include.inputSearch.setOnSearchClickListener {
                val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment()
                findNavController().navigate(action)
            }
            include.filterMenuAction.visibility = View.GONE
            include.clearFilterAction.visibility = View.GONE
            include.secondBar.visibility = View.GONE
            include.filterMenu.visibility = View.GONE

            floatingActionButtonAddItem.setOnClickListener {
                viewModel.setCurrentId(0)
                val action = MenuFragmentDirections.actionMenuFragmentToAddItemFragment2()
                findNavController().navigate(action)
            }

        }

    }

    override fun onResume() {
        lifecycleScope.launchWhenStarted {
            viewModel.mainMenuItemList.collect {
                idList.clear()
                it.forEachIndexed { index, menuItem ->
                    idList.add(mutableListOf<Long>())
                    menuItem.itemList?.forEach { item ->
                        idList[index].add(item.id)
                    }
                }
            }
        }
        super.onResume()
    }

    private fun subscribeUi(adapter: MenuListAdapter) {
        lifecycleScope.launchWhenStarted {
            viewModel.mainMenuItemList.collect {
                adapter.submitList(it)
                it.forEachIndexed { index, menuItem ->
                    idList.add(mutableListOf<Long>())
                    menuItem.itemList?.forEach { item ->
                        idList[index].add(item.id)
                    }
                }
            }
        }
    }

    override fun onItemClick(mainPosition: Int, childPosition: Int) {
        Log.d(
            "OnItemClick",
            "Menu Fragment: item Clicked - 2 variables $mainPosition - $childPosition"
        )
        if (childPosition > -1) {
            var currentItemId: Long? = idList[mainPosition][childPosition]
            if (currentItemId != null) {
                viewModel.setCurrentId(currentItemId!!)
                val action = MenuFragmentDirections.actionMenuFragmentToDetailFragment()
                this.findNavController().navigate(action)
            }
        } else {
            // TODO Navigate to List Fragment
        }
    }

}