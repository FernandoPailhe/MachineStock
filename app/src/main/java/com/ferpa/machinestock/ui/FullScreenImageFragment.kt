package com.ferpa.machinestock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
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
            bindPhotoViewPager(item, photoPosition)
        }
        
        

        binding.apply {

            deleteAction.setOnClickListener {
                //TODO Implement are you sure dialog
                viewModel.deletePhoto(item, fullScreenPhotoViewPager.currentItem)
            }
            setFirstImageAction.setOnClickListener {
                //TODO Implement reorganize photos
            }

        }

    }

    private fun bindPhotoViewPager(item: Item, photoPosition :Int) {
        val fullScreenPhotoAdapter = PhotoAdapter(item.getMachinePhotoList(), this)
        binding.fullScreenPhotoViewPager.adapter = fullScreenPhotoAdapter
        binding.fullScreenPhotoViewPager.currentItem = photoPosition
    }

    override fun onItemClick(position: Int) {
        //TODO Context Menu to share and edit photo?
    }

    companion object {
        const val TAG = "FullScreenImageFragment"
    }
}
