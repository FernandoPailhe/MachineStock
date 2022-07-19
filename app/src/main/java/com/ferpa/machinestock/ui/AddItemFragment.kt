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
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentAddItemBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.utilities.Const
import com.ferpa.machinestock.utilities.Const.REQUEST_GALLERY_PHOTO
import com.ferpa.machinestock.utilities.Const.REQUEST_TAKE_PHOTO
import com.ferpa.machinestock.utilities.imageUtils.ImageManager
import com.ferpa.machinestock.utilities.imageUtils.ImageManager.Companion.getReduceBitmapFromGallery
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddItemFragment : Fragment(R.layout.fragment_add_item), PhotoAdapter.OnItemClickListener {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    lateinit var item: Item
    private lateinit var newProduct: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get Navigation Values
        val id = viewModel.currentId.value

        //Set interface type
        if (id > 0) {
            setDetailItemInterface()
            newProduct = viewModel.currentItem.value?.product.toString()
        } else {
            newProduct = viewModel.getProduct()
            if (newProduct == "TODAS") {
                selectProductDialog(savedInstanceState)
            } else {
                setNewItemInterface(newProduct)
            }
        }

        /*
        Set autocomplete text arrayList
         */
        binding.apply {
            itemLocation.apply {
                setAdapter(setAdapterArray(R.array.location_options))
                inputType = InputType.TYPE_NULL
            }
            itemBrand.setAdapter(setAdapterArray(R.array.brand_options))
            itemType.apply {
                setAdapter(setAdapterArray(R.array.type_options))
                inputType = InputType.TYPE_NULL
            }
            itemStatus.apply {
                setAdapter(setAdapterArray(R.array.status_options))
                inputType = InputType.TYPE_NULL
            }
        }

    }

    private fun setNewItemInterface(newProduct: String) {
        bindFeatures(newProduct, null, null, "new")
        isEditable(true)
        binding.apply {
            itemProduct.setText(newProduct)
            galleryAction.visibility = View.GONE
            cameraAction.visibility = View.GONE
            recyclerViewLayout.visibility = View.GONE
            saveAction.setOnClickListener {
                addNewItem()
            }
            cancelAction.setOnClickListener {
                val action = AddItemFragmentDirections.actionAddItemFragmentToMenuFragment()
                it.findNavController().navigate(action)
            }
        }
    }

    private fun setDetailItemInterface() {
        viewModel.currentItem.observe(this.viewLifecycleOwner) { selectedItem ->
            item = selectedItem
            bindItemDetails(item)
            bindPhotoRecyclerView(item)
        }
        isEditable(true)
        setUpdateButtonInterface(true)
    }

    private fun setUpdateButtonInterface(updateInterface: Boolean) {
        if (updateInterface) {
            isEditable(true)
            binding.apply {
                saveAction.setIconResource(R.drawable.ic_save)
                saveAction.text = getString(R.string.save_action)
                saveAction.setOnClickListener {
                    updateItem()
                    setUpdateButtonInterface(false)
                }
                cancelAction.setOnClickListener {
                    val action = AddItemFragmentDirections.actionAddItemFragmentToDetailFragment()
                    it.findNavController().navigate(action)
                }
            }
        } else {
            isEditable(false)
            binding.apply {
                cameraAction.setOnClickListener {
                    getCameraInstance()
                }
                galleryAction.setOnClickListener {
                    getGalleryInstance()
                }
            }
        }
    }

    //Add Image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val ei = ExifInterface(viewModel.currentPhotoPath)
            val orientation = ei.getAttribute(ExifInterface.TAG_ORIENTATION)
            val uriList = ImageManager.getReduceBitmapListFromCamera(viewModel.currentPhotoPath,
                orientation?.toInt() ?: 0, requireContext())
            viewModel.uploadPhoto(uriList[0])
            viewModel.uploadPhoto(uriList[1], true)
        } else if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val image = data.data
                if (image != null) {
                    val uriList = ImageManager.getReducedBitmapListFromGallery(image, requireContext())
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

    private fun bindItemDetails(item: Item) {
        binding.apply {
            itemProduct.setText(item.product, TextView.BufferType.SPANNABLE)
            bindFeatures(item.product, item.feature1, item.feature2, item.feature3.toString())
            itemBrand.setText(item.brand, TextView.BufferType.SPANNABLE)
            itemInsideNumber.setText(item.insideNumber, TextView.BufferType.SPANNABLE)
            itemLocation.apply {
                setText(item.getLocation(), TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray(R.array.location_options))
            }
            itemType.apply {
                setText(item.getType(), TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray(R.array.type_options))
            }
            itemOwner2.setText(item.owner2.toString())
            itemOwner1.setText(item.owner1.toString())
            itemPrice.setText(item.getFormattedPrice(false))
            itemObservations.setText(item.getObservations())
            itemStatus.apply {
                setText(item.status, TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray(R.array.status_options))
            }
            if (item.currency == "USD") {
                itemPriceLabel.hint = "Precio en USD"
            }

            if (item.photos == "0") {
                recyclerViewLayout.visibility = View.GONE
            }

            galleryAction.isEnabled = true
            cameraAction.isEnabled = true

        }
    }

    private fun setAdapterArray(stringArray: Int): ArrayAdapter<String> {
        val array = resources.getStringArray(stringArray)
        return ArrayAdapter(requireContext(), R.layout.dropdown_item, array)
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
                binding.itemFeature2Label.setHint(R.string.item_width_torno_req)
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
                binding.itemFeature1Label.hint = getString(R.string.item_pulg_req)
                binding.itemFeature2.visibility = View.GONE
            }
            "SOLDADURA" -> {
                feature1?.let {
                    binding.itemFeature1.setText(
                        it.toString(),
                        TextView.BufferType.SPANNABLE
                    )
                }
                binding.itemFeature1Label.hint = getString(R.string.item_mig_req)
                binding.itemFeature2.visibility = View.GONE
            }
        }

        //TODO SetRemainsProductsBindFeatures

        if (feature3 != "null") {
            if (feature3 != "new") {
                binding.itemFeature3.setText(feature3.toString())
            } else {
                binding.itemFeature3Label.hint = getString(R.string.item_feature3_req)
            }
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

    private fun addNewItem() {

        if (isEntryValid()) {
            viewModel.addNewItem(
                newProduct,
                binding.itemType.text.toString(),
                binding.itemFeature1.text.toString(),
                binding.itemFeature2.text.toString(),
                binding.itemFeature3.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemBrand.text.toString(),
                binding.itemInsideNumber.text.toString(),
                binding.itemLocation.text.toString(),
                "$",
                binding.itemStatus.text.toString(),
                binding.itemOwner2.text.toString(),
                binding.itemOwner1.text.toString(),
                binding.itemObservations.text.toString()
            )
            //TODO This have to wait the response of the updating
            val action = AddItemFragmentDirections.actionAddItemFragmentToDetailFragment()
            this.findNavController().navigate(action)
        }

    }

    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.setUpdateItem(
                item,
                newProduct,
                binding.itemType.text.toString(),
                binding.itemFeature1.text.toString(),
                binding.itemFeature2.text.toString(),
                binding.itemFeature3.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemBrand.text.toString(),
                binding.itemInsideNumber.text.toString(),
                binding.itemLocation.text.toString(),
                "$",
                binding.itemStatus.text.toString(),
                binding.itemOwner2.text.toString(),
                binding.itemOwner1.text.toString(),
                binding.itemObservations.text.toString()
            )

            //TODO This have to wait the response of the updating
            val action = AddItemFragmentDirections.actionAddItemFragmentToDetailFragment()
            this.findNavController().navigate(action)
        }

    }

    private fun isEditable(editMode: Boolean) {
        binding.apply {
            itemProduct.isEnabled = false
            itemFeature1.isEnabled = editMode
            itemFeature2.isEnabled = editMode
            itemFeature3.isEnabled = editMode
            itemBrand.isEnabled = editMode
            itemType.isEnabled = editMode
            itemPrice.isEnabled = editMode
            itemInsideNumber.isEnabled = editMode
            itemLocation.isEnabled = editMode
            itemOwner2.isEnabled = editMode
            itemOwner1.isEnabled = editMode
            itemStatus.isEnabled = editMode
            itemObservations.isEnabled = editMode
        }
    }

    private fun isEntryValid(): Boolean {

        val isEntryValid = viewModel.isEntryValid(
            binding.itemProduct.text.toString(),
            binding.itemFeature1.text.toString(),
            binding.itemFeature2.text.toString(),
            binding.itemOwner1.text.toString(),
            binding.itemOwner2.text.toString(),
            binding.itemInsideNumber.text.toString()
        )

        if (isEntryValid == 1) {
            Toast.makeText(this.context, R.string.blank_item, Toast.LENGTH_SHORT).show()
        } else if (isEntryValid == 2) {
            Toast.makeText(this.context, R.string.invalid_owner, Toast.LENGTH_SHORT).show()
        }

        return (isEntryValid == 0)
    }

    /*
    Product dialog for new item
     */
    private fun selectProductDialog(savedInstanceState: Bundle?): Dialog {
        val arrayList = resources.getStringArray(R.array.product_options)
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.dialog_new_product_title)
                .setItems(
                    R.array.product_options,
                    DialogInterface.OnClickListener { dialog, which ->
                        newProduct = arrayList[which].toString()
                        setNewItemInterface(newProduct)
                    })
            builder.create()
            builder.setOnCancelListener{
                val action = AddItemFragmentDirections.actionAddItemFragmentToMenuFragment()
                this.findNavController().navigate(action)
            }
            builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /*
    Navigate to edit photo detail
    */
    override fun onItemClick(position: Int) {
        val action = AddItemFragmentDirections.actionAddItemFragmentToFullScreenImageFragment(
            position
        )
        this.findNavController().navigate(action)
    }

}

