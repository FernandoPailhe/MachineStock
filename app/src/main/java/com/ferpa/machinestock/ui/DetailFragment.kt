package com.ferpa.machinestock.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentDetailBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.utilities.Const.REQUEST_GALLERY_PHOTO
import com.ferpa.machinestock.utilities.Const.REQUEST_TAKE_PHOTO
import com.ferpa.machinestock.utilities.imageUtils.ImageManager
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

    lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDetailItemInterface()

    }

    /* Binding
     */
    private fun setDetailItemInterface() {
        viewModel.currentItem.observe(this.viewLifecycleOwner) { selectedItem ->
            item = selectedItem
            bindItemDetails(item)
        }
    }

    private fun bindItemDetails(item: Item) {

        binding.apply {
            bindTextView(itemProduct, item.product)
            bindTextView(itemBrand, item.brand)
            bindTextView(itemFeature1, item.getFeatures())
            bindTextView(itemFeature3, item.feature3)
            bindTextView(itemInsideNumber, item.getInsideNumber())
            bindTextView(itemLocation, item.getLocation())
            bindTextView(itemType, item.getType())
            bindTextView(itemPrice, item.getFormattedPrice(true))
            bindTextView(itemObservations, item.getObservations())
            bindTextView(itemStatus, item.status)

            itemOwner2.text = item.owner2.toString()
            itemOwner1.text = item.owner1.toString()

            photoViewPager.adapter = PhotoAdapter(item.getMachinePhotoList(), this@DetailFragment)

            floatingActionButtonEditItem.setOnClickListener {
                viewModel.setCurrentId(item.id)
                val action =
                    DetailFragmentDirections.actionDetailFragmentToAddItemFragment(item.product)
                it.findNavController().navigate(action)
            }

            floatingActionButtonGallery.setOnClickListener {
                getGalleryInstance()
            }

            floatingActionButtonCamera.setOnClickListener {
                getCameraInstance()
            }

            floatingActionButtonShare.setOnClickListener {
                shareProduct()
            }

        }

    }

    private fun bindTextView(txtView: TextView, itemDetail: String?) {
        if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail == "") || (itemDetail.equals(
                "NO ESPECÍFICA"
            ))
        ) {
            txtView.visibility = View.GONE
        } else {
            txtView.text = itemDetail
        }
    }

    /*
    Share Information
    */
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

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray)
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, getShareIndexCard(true))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tittle))
            type = "image/png"
        }

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
            indexCard += resources.getString(
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
                item.getFormattedPrice(true)
            )
        }

        return indexCard

    }

    private fun getUriArray(machinePhotoList: List<MachinePhoto>): ArrayList<Uri> {

        val uriArray = ArrayList<Uri>()

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
            Log.e(TAG, "getUriFromFileToShare " + e.message)
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

    /*
    Upload Images
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val ei = ExifInterface(viewModel.currentPhotoPath)
            val orientation = ei.getAttribute(ExifInterface.TAG_ORIENTATION)
            val uriList = ImageManager.getReduceBitmapListFromCamera(
                viewModel.currentPhotoPath,
                orientation?.toInt() ?: 0, requireContext()
            )
            viewModel.uploadPhoto(uriList[0])
            viewModel.uploadPhoto(uriList[1], true)
        } else if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val image = data.data
                if (image != null) {
                    val uriList =
                        ImageManager.getReducedBitmapListFromGallery(image, requireContext())
                    viewModel.uploadPhoto(uriList[0])
                    viewModel.uploadPhoto(uriList[1], true)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getCameraInstance() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            context?.let {
                takePictureIntent.resolveActivity(it.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also { file ->
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this.requireContext(),
                            "com.ferpa.fileprovider",
                            file
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)

                    }
                }
            }
        }

    }

    private fun getGalleryInstance() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, REQUEST_GALLERY_PHOTO)
        }
    }

    /*
    Navigate To Full Screen Fragment
     */
    override fun onItemClick(position: Int) {

        if (item.getMachinePhotoList()[0].id == -1) {
            getGalleryInstance()
        } else {
            val action = DetailFragmentDirections.actionDetailFragmentToFullScreenImageFragment(
                position
            )
            this.findNavController().navigate(action)
        }
    }

    companion object {
        const val TAG = "DetailFragment"
    }

}