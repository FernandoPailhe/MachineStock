package com.ferpa.machinestock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.databinding.FragmentFullScreenImageBinding
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.getMachinePhotoList
import com.ferpa.machinestock.ui.adapter.FullScreenPhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel



class FullScreenImageFragment : Fragment(), FullScreenPhotoAdapter.OnItemClickListener {


    private val navigationArgs: FullScreenImageFragmentArgs by navArgs()

    private var _binding: FragmentFullScreenImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullScreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = navigationArgs.itemId

        val photoId = navigationArgs.photoId

        viewModel.retrieveItem(itemId).observe(this.viewLifecycleOwner){ selectedItem ->
            item = selectedItem
            bindPhotoRecyclerView(item, photoId)
        }

    }

    private fun bindPhotoRecyclerView(item: Item, photoId :Int) {
        val fullScreenPhotoAdapter = FullScreenPhotoAdapter(item.getMachinePhotoList(), this)

        binding.fullScreenPhotoRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.fullScreenPhotoRecyclerView.adapter = fullScreenPhotoAdapter
        binding.fullScreenPhotoRecyclerView.scrollToPosition(photoId)
    }

    override fun onItemClick(position: Int) {
        val action =
            FullScreenImageFragmentDirections.actionFullScreenImageFragmentToAddItemFragment(item.product, item.id)
        this.findNavController().navigate(action)
    }
}
