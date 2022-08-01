package com.ferpa.machinestock.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.databinding.FragmentMenuBinding
import com.ferpa.machinestock.ui.adapter.ItemListAdapter
import com.ferpa.machinestock.ui.adapter.MenuListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

        if(Firebase.auth.currentUser == null) {
            val action = MenuFragmentDirections.actionMenuFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        menuListAdapter = MenuListAdapter(this)

        lifecycleScope.launchWhenStarted {
            viewModel.isDbUpdate.collect {
                if (it) {
                    subscribeUi(menuListAdapter)
                } else {
                    Toast.makeText(this@MenuFragment.requireContext(), R.string.db_update_process, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply{
            menuRecyclerView.adapter = menuListAdapter
            menuRecyclerView.layoutManager =
                LinearLayoutManager(this@MenuFragment.requireContext())

            editListAction.setOnClickListener {
                viewModel.setEditList(true)
                val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment()
                findNavController().navigate(action)
            }

            searchAction.setOnClickListener {
                viewModel.setEditList(false)
                val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment()
                findNavController().navigate(action)
            }

            updateAction.setOnClickListener {
                viewModel.compareDatabases()
            }

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
        Toast.makeText(this.context, R.string.db_updated, Toast.LENGTH_LONG).show()
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