package com.ferpa.machinestock.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentFullScreenImageBinding
import com.ferpa.machinestock.model.Item
import com.ferpa.machinestock.model.getMachinePhotoList
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.FullScreenImageViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FullScreenImageFragment : Fragment(R.layout.fragment_full_screen_image), PhotoAdapter.OnItemClickListener {

    private var _binding: FragmentFullScreenImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FullScreenImageViewModel by viewModels()

    private val navArgs: FullScreenImageFragmentArgs by navArgs()

    lateinit var machine: Item

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

        viewModel.getMachine(navArgs.machineId)

        val photoPosition = navArgs.photoPosition

        viewModel.machine.observe(this.viewLifecycleOwner){ selectedItem ->
            machine = selectedItem
            bindPhotoViewPager(machine, photoPosition)
        }

        binding.apply {

            deleteAction.setOnClickListener {
                val builder = AlertDialog.Builder(this@FullScreenImageFragment.context)
                builder.setMessage(R.string.dialog_delete_photo)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_yes) { _, _ ->
                        viewModel.deletePhoto(machine, fullScreenPhotoViewPager.currentItem)
                    }
                    .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()

            }

            setFirstImageAction.visibility = View.GONE

        }

    }

    private fun bindPhotoViewPager(machine: Item, photoPosition :Int) {
        val fullScreenPhotoAdapter = PhotoAdapter(machine.product, machine.getMachinePhotoList(), this)
        binding.fullScreenPhotoViewPager.adapter = fullScreenPhotoAdapter
        binding.fullScreenPhotoViewPager.currentItem = photoPosition
    }

    override fun onItemClick(position: Int) {

    }
}
