package com.ferpa.machinestock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentFullScreenImageBinding
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.getMachinePhotoList
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FullScreenImageFragment : Fragment(R.layout.fragment_full_screen_image), PhotoAdapter.OnItemClickListener {


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

        val itemId = viewModel.currentId.value

        val photoPosition = navigationArgs.photoPosition

        viewModel.retrieveItem(itemId).observe(this.viewLifecycleOwner){ selectedItem ->
            item = selectedItem
            bindPhotoRecyclerView(item, photoPosition)
        }

    }

    private fun bindPhotoRecyclerView(item: Item, photoPosition :Int) {
        val fullScreenPhotoAdapter = PhotoAdapter(item.getMachinePhotoList(), this)
        binding.fullScreenPhotoViewPager.adapter = fullScreenPhotoAdapter
        /* TODO set initial position
        binding.fullScreenPhotoViewPager.scrollToPosition(photoId)
         */
    }

    override fun onItemClick(position: Int) {
        val action =
            FullScreenImageFragmentDirections.actionFullScreenImageFragmentToAddItemFragment(item.product)
        this.findNavController().navigate(action)
    }
}
