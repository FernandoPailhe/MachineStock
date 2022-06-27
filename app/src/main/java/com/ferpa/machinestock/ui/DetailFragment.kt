package com.ferpa.machinestock.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentDetailBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail), PhotoAdapter.OnItemClickListener {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    private val REQUEST_GALLERY_PHOTO = 199
    private val REQUEST_TAKE_PHOTO = 198

    lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDetailItemInterface()

    }

    private fun setDetailItemInterface() {
        viewModel.currentItem.observe(this.viewLifecycleOwner) { selectedItem ->
            item = selectedItem
            bindItemDetails(item)
            bindPhotoRecyclerView(item)
        }
    }

    private fun bindItemDetails(item: Item) {

        binding.apply {
            detailCard.setBackgroundResource(item.getBackgroundColor())
            bindTextView(itemProduct, item.product)
            bindTextView(itemBrand, item.brand)
            bindTextView(itemFeature1, item.getFeatures())
            bindTextView(itemFeature3, item.feature3)
            bindTextView(itemInsideNumber, item.getInsideNumber())
            bindTextView(itemLocation, item.getLocation())
            bindTextView(itemType, item.getType())
            bindTextView(itemPrice, item.getFormattedPrice())
            bindTextView(itemObservations, item.getObservations())
            bindTextView(itemStatus, item.status)

            itemOwner2.text = item.owner2.toString()
            itemOwner1.text = item.owner1.toString()

            if (item.photos == "0") {
                photoCard.visibility = View.GONE
            } else {
                bindPhotoRecyclerView(item)
            }

            editAction.setOnClickListener {
                viewModel.setCurrentId(item.id)
                val action = DetailFragmentDirections.actionDetailFragmentToAddItemFragment(item.product)
                it.findNavController().navigate(action)
            }

            shareAction.setOnClickListener {
                shareProduct()
            }

        }
    }

    private fun bindTextView(txtView: TextView, itemDetail: String?) {
        if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail.equals("NO ESPECÍFICA"))) {
            txtView.visibility = View.GONE
        } else {
            txtView.text = itemDetail
        }
    }

    private fun bindPhotoRecyclerView(item: Item) {

        if (item.getMachinePhotoList().isEmpty()) {
            binding.photoRecyclerView.visibility = View.GONE
        } else {
            val photoAdapter = PhotoAdapter(item.getMachinePhotoList(), this)
            binding.photoRecyclerView.layoutManager =
                LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            binding.photoRecyclerView.adapter = photoAdapter
        }

    }

    //Share Information
    private fun shareProduct() {

        if (item.getMachinePhotoList().isEmpty()) {
            shareIndexCard(getShareIndexCard(true))
        } else {
            shareIndexCardWithImages(item.getMachinePhotoList())
        }
    }

    private fun shareIndexCard(message: String) {

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_TITLE, getString(R.string.share_tittle))
        }
        startActivity(Intent.createChooser(intent, null))

    }

    private fun shareIndexCardWithImages(machinePhotoList: List<MachinePhoto>) {

        val uriArray = getUriArray(machinePhotoList)

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray)
        intent.type = "image/*"

        // adding text to share
        intent.putExtra(Intent.EXTRA_TEXT, getShareIndexCard(true))

        // Add subject Here
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tittle))

        // setting type to image
        intent.type = "image/png"

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, getString(R.string.share_tittle)))

    }

    private fun getShareIndexCard(withPrice: Boolean): String {

        var indexCard = resources.getString(R.string.index_card_product, item.product)

        if (item.brand != null) {
            indexCard += resources.getString(
                R.string.index_card_brand,
                item.brand
            )
        }
        if (item.feature1 != null) {
            indexCard += resources.getString(
                R.string.index_card_features,
                item.getFeatures()
            )
        }
        if (item.feature3 != null) {
            indexCard = resources.getString(
                R.string.index_card_other_features,
                item.feature3
            )
        }
        if (item.type != null) {
            indexCard += resources.getString(
                R.string.index_card_type,
                item.getType()
            )
        }
        if (withPrice) {
            indexCard += resources.getString(
                R.string.index_card_price,
                item.getFormattedPrice()
            )
        }

        return indexCard

    }

    private fun getUriArray(machinePhotoList: List<MachinePhoto>): ArrayList<Uri> {

        var uriArray = ArrayList<Uri>()

        for (photo in machinePhotoList) {

            val file = getLocalFileToShare(photo.imgSrcUrl.toString())
            uriArray.add(getUriFromFileToShare(file)!!)

        }

        return uriArray

    }

    private fun getLocalFileToShare(imgSrcUrl: String): File {
        //TODO Change this to use Picasso Cache, probably using Okhttp3
        val localFile = createImageFile()
        FirebaseStorage.getInstance().reference.child(imgSrcUrl)
            .getFile(
                localFile
            ).addOnSuccessListener {

            }.addOnFailureListener {
                Log.d("Firestorage", "Fail get image to share $it")
            }

        return localFile
    }

    private fun getUriFromFileToShare(file: File): Uri? {

        var uri: Uri? = null
        try {
            uri = FileProvider.getUriForFile(
                this.requireContext(),
                "com.ferpa.fileprovider", file
            )
        } catch (e: java.lang.Exception) {
            Toast.makeText(this.requireContext(), "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            viewModel.currentPhotoPath = absolutePath
        }
    }

    //Navigate To Photo Detail
    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

}