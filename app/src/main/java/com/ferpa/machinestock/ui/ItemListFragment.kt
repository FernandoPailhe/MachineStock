package com.ferpa.machinestock.ui


import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentItemListBinding
import com.ferpa.machinestock.ui.adapter.MiniCardSearchListAdapter
import com.ferpa.machinestock.ui.viewmodel.ItemListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemListFragment : Fragment(R.layout.fragment_item_list),
    androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private lateinit var productListAdapter: MiniCardSearchListAdapter

    private lateinit var allItemsListAdapter: MiniCardSearchListAdapter

    private val viewModel: ItemListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isFilterMenuVisible = false

        allItemsListAdapter = MiniCardSearchListAdapter ({ machine ->
            val action =
                com.ferpa.machinestock.ui.ItemListFragmentDirections.actionItemListFragmentToDetailFragment(machineId = machine.id)
            this.findNavController().navigate(action)
        }, true)

        productListAdapter = MiniCardSearchListAdapter ({ machine ->
            val action =
                ItemListFragmentDirections.actionItemListFragmentToDetailFragment(machineId = machine.id)
            this.findNavController().navigate(action)
        })

        bindRecyclerView(viewModel.getProduct())

        binding.apply {

            recyclerView.layoutManager = LinearLayoutManager(this@ItemListFragment.context)

            /*
            Top Menu
             */
            listTitle.apply {
                inputType = InputType.TYPE_NULL
                setText(viewModel.getProduct(), TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray())
                setOnItemClickListener { adapterView, view, position, l ->
                    viewModel.setProduct(adapterView.getItemAtPosition(position) as String)
                    bindRecyclerView(adapterView.getItemAtPosition(position) as String)
                }
            }
            inputSearch.setOnQueryTextListener(this@ItemListFragment)
            clearFilterAction.setOnClickListener {
                clearAllFilters()
            }
            /*
            Filter Menu
             */
            setFilterMenuVisibility(isFilterMenuVisible)
            filterMenuAction.setOnClickListener {
                setFilterMenuVisibility(!isFilterMenuVisible)
                isFilterMenuVisible = !isFilterMenuVisible
            }
            bindFilterMenu()

        }

        lifecycleScope.launchWhenStarted {
            viewModel.isNewFilter.collect {
                if (it) {
                    viewModel.collectFilterList()
                    Log.d(TAG, "IsNewFilter")
                    Log.d(TAG, "New Product ${viewModel.getProduct()}")
                    if (viewModel.getProduct() == "TODAS") {
                        subscribeUiAllItems(allItemsListAdapter)
                    } else {
                        subscribeUi(productListAdapter)
                    }
                    viewModel.setNewFilterFalse()
                }
            }
        }

    }

    override fun onResume() {

        binding.listTitle.apply {
            inputType = InputType.TYPE_NULL
            setText(viewModel.getProduct(), TextView.BufferType.SPANNABLE)
            setAdapter(setAdapterArray())
            setOnItemClickListener { adapterView, view, position, l ->
                viewModel.setProduct(adapterView.getItemAtPosition(position) as String)
                bindRecyclerView(adapterView.getItemAtPosition(position) as String)
            }
        }
        super.onResume()
    }

    private fun setFilterMenuVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.filterMenu.startLayoutAnimation()
            binding.filterMenu.visibility = View.VISIBLE
        } else {
            binding.filterMenu.visibility = View.GONE
        }

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.setQueryText(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.setQueryText(newText)
        return true
    }

    private fun subscribeUi(adapter: MiniCardSearchListAdapter) {
        viewModel.filterList.observe(this.viewLifecycleOwner) { filterList ->
            Log.d(TAG, "subscribeUiAll -> ${filterList.size} items")
            adapter.submitList(filterList)
        }
        binding.recyclerView.scrollToPosition(viewModel.itemListPosition.value)
    }

    private fun subscribeUiAllItems(adapter: MiniCardSearchListAdapter) {
        viewModel.filterList.observe(this.viewLifecycleOwner) { filterList ->
            Log.d(TAG, "subscribeUiAll -> ${filterList.size} items")
            adapter.submitList(filterList)
        }
        binding.recyclerView.scrollToPosition(viewModel.itemListPosition.value)
    }

    private fun bindFilter(tv: CheckBox, type: Int, position: Int) {

        val typeStr =
            getStringFromArray(type, position).lowercase().replaceFirstChar { it.uppercase() }
        tv.text = typeStr
        tv.isChecked = viewModel.getFilterStatus(typeStr)
        tv.setOnClickListener {
            viewModel.setFilter(typeStr)
            checkFilter()
        }

    }

    private fun bindRecyclerView(product: String) {
        if (product == "TODAS") {
            binding.recyclerView.adapter = allItemsListAdapter
        } else {
            binding.recyclerView.adapter = productListAdapter
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                viewModel.itemListPosition.value =
                    (((recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!))
                Log.d(TAG, "set scroll with AddOnScroll to ${viewModel.itemListPosition.value}")
            }
        })

    }

    private fun bindFilterMenu() {
        binding.apply {

            //FilterOwner
            bindFilter(filterOwner1, R.array.owner_options, 0)
            bindFilter(filterOwner2, R.array.owner_options, 1)
            bindFilter(filterShared, R.array.owner_options, 2)
            filterOwner1.visibility = View.GONE
            filterOwner2.visibility = View.GONE
            filterShared.visibility = View.GONE
            divider1.visibility = View.GONE

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

    private fun checkFilter() {
        if (viewModel.isFilterList()) {
            binding.clearFilterAction.isEnabled = true
            binding.clearFilterAction.visibility = View.VISIBLE
            binding.filterMenuAction.setImageResource(R.drawable.ic_baseline_filter_alt_24_color)
        } else {
            binding.clearFilterAction.isEnabled = false
            binding.clearFilterAction.visibility = View.GONE
            binding.filterMenuAction.setImageResource(R.drawable.ic_baseline_filter_alt_24)
        }
    }

    private fun clearAllFilters() {
        viewModel.clearFilters()
        checkFilter()
        bindFilterMenu()
    }

    private fun setAdapterArray(): ArrayAdapter<String> {
        val array = arrayListOf("TODAS")
        viewModel.productArray.observe(this.viewLifecycleOwner) {
            for (product in it) {
                array.add(product)
            }
        }
        return ArrayAdapter(requireContext(), R.layout.dropdown_item, array)
    }

    companion object {
        const val TAG = "ItemListFragment"
    }

}
