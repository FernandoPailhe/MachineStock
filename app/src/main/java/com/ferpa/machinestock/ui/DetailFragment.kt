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
            itemProduct.setText(item.product, TextView.BufferType.SPANNABLE)
            bindFeatures(item.product, item.feature1, item.feature2, item.feature3.toString())
            itemBrand.setText(item.brand, TextView.BufferType.SPANNABLE)
            itemInsideNumber.setText(item.insideNumber, TextView.BufferType.SPANNABLE)
            itemLocation.setText(item.getLocation(), TextView.BufferType.SPANNABLE)
            itemType.setText(item.getType(), TextView.BufferType.SPANNABLE)

            itemOwner2.setText(item.owner2.toString())
            itemOwner1.setText(item.owner1.toString())
            itemPrice.setText(item.getFormattedPrice())
            itemObservations.setText(item.getObservations())
            itemStatus.setText(item.status, TextView.BufferType.SPANNABLE)
            if (item.currency == "USD") {
                itemPriceLabel.hint = "Precio en USD"
            }
            if (item.photos == "0") {
                recyclerViewLayout.visibility = View.GONE
            }

        }
    }

    private fun bindFeatures(
        product: String,
        feature1: Double?,
        feature2: Double?,
        feature3: String?
    ) {

        when (product) {
            "GUILLOTINA" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(R.string.item_length_req)
                feature2?.let {
                    binding.itemFeature2.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature2Label.setHint(R.string.item_width_guillotina_req)
            }
            "PLEGADORA" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(R.string.item_length_req)
                feature2?.let {
                    binding.itemFeature2.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature2Label.setHint(R.string.item_weight_req)
            }
            "BALANCIN" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(R.string.item_weight_req)
                binding.itemFeature2.visibility = View.GONE
            }
            "TORNO" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(R.string.item_length_req)
                feature2?.let {
                    binding.itemFeature2.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature2Label.setHint(R.string.item_width_guillotina_req)
            }
            "LIMADORA" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(R.string.item_length_req)
                binding.itemFeature2.visibility = View.GONE
            }
            "SERRUCHO" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(getString(R.string.item_pulg_req))
                binding.itemFeature2.visibility = View.GONE
            }
            "SOLDADURA" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.setHint(getString(R.string.item_mig_req))
                binding.itemFeature2.visibility = View.GONE
            }
        }

        //TODO SetRemainsProductsBindFeatures

        if (feature3 != "null") {
            if (feature3 != "new") {
                binding.itemFeature3.setText(feature3.toString())
            }
        } else {
            binding.itemFeature3Label.visibility = View.GONE
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