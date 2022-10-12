package com.ferpa.machinestock.ui


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentMenuBinding
import com.ferpa.machinestock.model.MachineStockUser
import com.ferpa.machinestock.ui.adapter.ItemListAdapter
import com.ferpa.machinestock.ui.adapter.MenuListAdapter
import com.ferpa.machinestock.ui.viewmodel.MenuViewModel
import com.ferpa.machinestock.utilities.Const.NEW_ITEM
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : Fragment(R.layout.fragment_menu), MenuListAdapter.OnItemClickListener {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels()

    private var idList: MutableList<MutableList<Long>> = mutableListOf()

    private var menuList: MutableList<String> = mutableListOf()

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

        /*
        if (Firebase.auth.currentUser == null) {
            val action = MenuFragmentDirections.actionMenuFragmentToLogInFragment()
            findNavController().navigate(action)
        } else {
            val user = Firebase.auth.currentUser
            if (user != null) {
                viewModel.createUser(
                    MachineStockUser(
                        user.uid,
                        name = user.displayName,
                        profilePhotoUrl = user.photoUrl.toString(),
                        email = user.email,
                        phoneNumber = user.phoneNumber
                    )
                )
            }
        }
         */

        menuListAdapter = MenuListAdapter(this)


        subscribeUi(menuListAdapter)


        binding.apply {
            menuRecyclerView.adapter = menuListAdapter
            menuRecyclerView.layoutManager =
                LinearLayoutManager(this@MenuFragment.requireContext())
            menuRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    viewModel.setMenuFragmentRecyclerViewPosition(
                        (menuRecyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!,
                        0
                    )
                    Log.d(TAG, "set scroll with AddOnScroll to ${viewModel.menuPosition.value}")
                }
            })

            editListAction.visibility = View.GONE

            searchAction.setOnClickListener {
                val action = MenuFragmentDirections.actionMenuFragmentToItemListFragment()
                findNavController().navigate(action)
            }

            addItemAction.setOnClickListener {
                val action = MenuFragmentDirections.actionMenuFragmentToAddItemFragment2(NEW_ITEM)
                findNavController().navigate(action)
            }

        }

    }

    override fun onResume() {
        viewModel.mainMenuMachineList.observe(viewLifecycleOwner, Observer { mainMenuList ->
            idList.clear()
            mainMenuList.forEachIndexed { index, menuItem ->
                idList.add(mutableListOf())
                menuItem.itemList?.forEach { item ->
                    idList[index].add(item.id)
                }
            }
            val position = viewModel.menuPosition.value[0]
            binding.menuRecyclerView.scrollToPosition(position)
        })
        super.onResume()
    }

    override fun onDestroyView() {
        lifecycleScope.launch {
            viewModel.updateMainMenuPreferences()
        }
        super.onDestroyView()
    }

    private fun subscribeUi(adapter: MenuListAdapter) {
        viewModel.mainMenuMachineList.observe(this.viewLifecycleOwner) { mainMenuList ->
            adapter.submitList(mainMenuList)
            mainMenuList.forEachIndexed { index, menuItem ->
                idList.add(mutableListOf())
                menuList.add(menuItem.name)
                menuItem.itemList?.forEach { item ->
                    idList[index].add(item.id)
                }
            }
        }
    }

    override fun onItemClick(mainPosition: Int, childPosition: Int, expand: Boolean) {
        Log.d(
            "OnItemClick",
            "Menu Fragment: item Clicked - 2 variables $mainPosition - $childPosition"
        )
        if (childPosition > -1) {
            val currentItemId: Long = idList[mainPosition][childPosition]
            viewModel.setMenuFragmentRecyclerViewPosition(mainPosition, childPosition)
            val action =
                MenuFragmentDirections.actionMenuFragmentToDetailFragment(currentItemId)
            this.findNavController().navigate(action)
        } else {
            Log.d(TAG, "Click on: ${menuList[mainPosition]}, set $expand")
            viewModel.setExpand(expand, menuList[mainPosition])
        }
    }

    companion object {
        const val TAG = "MenuFragment"
    }

}