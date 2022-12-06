package com.ferpa.machinestock.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentDetailBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.DetailViewModel
import com.ferpa.machinestock.utilities.Const.REQUEST_CODE_GALLERY_PHOTO
import com.ferpa.machinestock.utilities.Const.REQUEST_CODE_TAKE_PHOTO
import com.ferpa.machinestock.utilities.PhotoListManager
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

    private val viewModel: DetailViewModel by viewModels()

    private val navArgs: DetailFragmentArgs by navArgs()

    lateinit var currentMachine: Item

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

        binding.editStatusAction.setOnClickListener{
            selectStatusDialog(savedInstanceState)
        }

    }

    override fun onResume() {
        setDetailItemInterface()
        super.onResume()
    }

    /*
    Binding
     */
    private fun setDetailItemInterface() {
        viewModel.getMachine(navArgs.machineId)
        viewModel.machine.observe(this.viewLifecycleOwner) { machine ->
            currentMachine = machine
            bindItemDetails(machine)
        }
    }

    private fun bindItemDetails(machine: Item) {

        binding.apply {
            bindTextView(itemProduct, machine.product)
            bindTextView(itemBrand, machine.getBrand())
            bindTextView(itemFeature1, machine.getFeatures())
            bindTextView(itemFeature3, machine.feature3)
            bindTextView(itemInsideNumber, machine.getInsideNumber())
            bindTextView(itemLocation, machine.getLocation())
            bindTextView(itemType, machine.getType())
            bindTextView(itemPrice, machine.getFormattedPrice(true))
            bindTextView(itemObservations, machine.getObservations())
            bindTextView(itemStatus, machine.status)
            bindTextView(itemEditUser, machine.getEditUser())

            itemOwner.text = machine.getOwnership()

            photoViewPager.adapter = PhotoAdapter(machine.product, PhotoListManager.getMachinePhotoList(machine), this@DetailFragment)

            floatingActionButtonEditItem.setOnClickListener {
                val action =
                    DetailFragmentDirections.actionDetailFragmentToAddItemFragment(product = machine.product, machineId = navArgs.machineId)
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
        if ((itemDetail.equals("null")) || (itemDetail == null) || (itemDetail == " ") || (itemDetail == "")) {
            txtView.visibility = View.GONE
        } else {
            txtView.text = itemDetail
        }
    }

    /*
    Share Information
    */
    private fun shareProduct() {

        var withPrice = true

        if (currentMachine.photos == "0") {
            shareIndexCard(getShareIndexCard(withPrice))
        } else {
            shareIndexCardWithImages(currentMachine.getMachinePhotoList(), withPrice)
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

    private fun shareIndexCardWithImages(machinePhotoList: List<MachinePhoto>, withPrice: Boolean?) {

        val uriArray = getUriArray(machinePhotoList)

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArray)
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, getShareIndexCard(withPrice))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tittle))
            type = "image/png"
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_tittle)))

    }

    private fun getShareIndexCard(withPrice: Boolean?): String {

        var indexCard = resources.getString(R.string.index_card_product, currentMachine.product)

        if (!currentMachine.brand.isNullOrEmpty()) {
            indexCard += resources.getString(
                R.string.index_card_brand,
                currentMachine.getBrand()
            )
        }
        if (currentMachine.feature1 != null || currentMachine.feature2 != null) {
            indexCard += resources.getString(
                R.string.index_card_features,
                currentMachine.getFeatures()
            )
        }
        if (!currentMachine.feature3.isNullOrEmpty()) {
            indexCard += resources.getString(
                R.string.index_card_other_features,
                currentMachine.feature3
            )
        }
        if (!currentMachine.type.isNullOrEmpty() && currentMachine.type != " ") {
            indexCard += resources.getString(
                R.string.index_card_type,
                currentMachine.getType()
            )
        }
        if (withPrice != null){
            if (withPrice) {
                indexCard += resources.getString(
                    R.string.index_card_price,
                    currentMachine.getFormattedPrice(true)
                )
            }
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

        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val ei = ExifInterface(viewModel.currentPhotoPath)
            val orientation = ei.getAttribute(ExifInterface.TAG_ORIENTATION)
            val uriList = ImageManager.getReduceBitmapListFromCamera(
                viewModel.currentPhotoPath,
                orientation?.toInt() ?: 0, requireContext()
            )
            viewModel.uploadPhoto(uriList[0])
            viewModel.uploadPhoto(uriList[1], true)
        } else if (requestCode == REQUEST_CODE_GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
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
                        startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO)

                    }
                }
            }
        }

    }

    private fun getGalleryInstance() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, REQUEST_CODE_GALLERY_PHOTO)
        }
    }

    /*
    Navigate To Full Screen Fragment
     */
    override fun onItemClick(position: Int) {

        if (currentMachine.getMachinePhotoList()[0].id == -1) {
            getGalleryInstance()
        } else {
            val action = DetailFragmentDirections.actionDetailFragmentToFullScreenImageFragment(
                machineId = currentMachine.id,
                photoPosition = position
            )
            this.findNavController().navigate(action)
        }
    }

    /*
    New status dialog
     */
    private fun selectStatusDialog(savedInstanceState: Bundle?): Dialog {
        val arrayList = resources.getStringArray(R.array.status_options)
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.dialog_new_status)
                .setItems(
                    R.array.status_options,
                    DialogInterface.OnClickListener { _, which ->
                        viewModel.setUpdateStatus(arrayList[which].toString())
                    })
            builder.create()
            builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "DetailFragment"
    }

}