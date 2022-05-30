package com.ferpa.machinestock.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.MachineStockApplication
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentItemListBinding
import com.ferpa.machinestock.ui.adapter.ItemListAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import kotlinx.coroutines.launch


class ItemListFragment : Fragment(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private val navigationArgs: ItemListFragmentArgs by navArgs()

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemListAdapter

    private val viewModel: MachineStockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isFilterMenuVisible = false

        viewModel.setProduct(navigationArgs.product)

        adapter = ItemListAdapter {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddItemFragment(
                it.product,
                it.id
            )
            this.findNavController().navigate(action)
        }

        binding.apply {
            bViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            recyclerView.layoutManager = LinearLayoutManager(this@ItemListFragment.context)
            recyclerView.adapter = adapter

            //Top Menu
            listTitle.text = navigationArgs.product
            inputSearch.setOnQueryTextListener(this@ItemListFragment)
            addItemAction.setOnClickListener() {
                findNavController().navigate(
                ItemListFragmentDirections.actionItemListFragmentToAddItemFragment(navigationArgs.product)
                )
            }
            clearFilterAction.setOnClickListener(){
                clearAllFilters()
            }
            //Filter Menu
            setFilterMenuVisibility(isFilterMenuVisible)
            filterMenuAction.setOnClickListener(){
                setFilterMenuVisibility(!isFilterMenuVisible)
                isFilterMenuVisible = !isFilterMenuVisible
            }
            bindFilterMenu()

        }

        lifecycleScope.launch {
            viewModel.isNewFilter.collect{
                if (it){
                    subscribeUi(adapter)
                    viewModel.setNewFilterFalse()
                }
            }
        }

    }

    private fun setFilterMenuVisibility(isVisible: Boolean){
        if (isVisible){
            binding.filterMenu.startLayoutAnimation()
            binding.filterMenu.visibility = View.VISIBLE
        } else {
            binding.filterMenu.visibility = View.GONE
        }

    }

    //TODO Hide filter menu with swipeUp

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.setQueryText(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.setQueryText(newText)
        return true
    }

    private fun subscribeUi(adapter: ItemListAdapter) {
        lifecycleScope.launchWhenStarted {
            viewModel.filterItems.collect(){
                adapter.submitList(it)
            }
        }
    }

    private fun bindFilter(tv: CheckBox, type: Int, position: Int){

        val typeStr = getStringFromArray(type, position)
        tv.text = typeStr
        tv.isChecked = viewModel.getFilterStatus(typeStr)
        tv.setOnClickListener {
            viewModel.setFilter(typeStr)
            checkFilter()
        }

    }

    private fun bindFilterMenu(){
        binding.apply {

            //FilterOwner
            bindFilter(filterOwner1, R.array.owner_options, 0)
            bindFilter(filterOwner2, R.array.owner_options, 1)
            bindFilter(filterShared, R.array.owner_options, 2)

            //FilterType
            bindFilter(filterType1, R.array.type_options, 0)
            bindFilter(filterType2, R.array.type_options, 1)
            bindFilter(filterType3, R.array.type_options, 2)

            //FilterState
            bindFilter(filterStatus1, R.array.status_options, 0)
            bindFilter(filterStatus2, R.array.status_options, 1)
            bindFilter(filterStatus3, R.array.status_options, 2)
            bindFilter(filterStatus4, R.array.status_options, 3)
            bindFilter(filterStatus5, R.array.status_options, 4)
            bindFilter(filterStatus6, R.array.status_options, 5)

            checkFilter()
        }

    }

    private fun getStringFromArray(stringArray: Int, position: Int): String {
        val array = resources.getStringArray(stringArray)
        return array[position]
    }

    private fun checkFilter(){
        if (viewModel.isFilterList()){
            binding.clearFilterAction.isEnabled = true
            binding.clearFilterAction.visibility = View.VISIBLE
            binding.filterMenuAction.setImageResource(R.drawable.ic_baseline_filter_alt_24_color)
        } else {
            binding.clearFilterAction.isEnabled = false
            binding.clearFilterAction.visibility = View.GONE
            binding.filterMenuAction.setImageResource(R.drawable.ic_baseline_filter_alt_24)
        }
    }

    private fun clearAllFilters(){
        viewModel.clearFilters()
        checkFilter()
        bindFilterMenu()
    }

}
