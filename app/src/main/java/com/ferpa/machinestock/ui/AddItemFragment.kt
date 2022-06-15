package com.ferpa.machinestock.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentAddItemBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel
import com.ferpa.machinestock.utilities.ImageResizer
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AddItemFragment : Fragment(), PhotoAdapter.OnItemClickListener {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    private val REQUEST_GALLERY_PHOTO = 199
    private val REQUEST_TAKE_PHOTO = 198
    lateinit var currentPhotoPath: String

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
        newProduct = viewModel.getProduct()
        val id = viewModel.currentId.value


        if (newProduct == "TODAS") {
            selectProductDialog(savedInstanceState)
        }

        //Set interface type
        if (id > 0) {
            setDetailItemInterface()
        } else {
            setNewItemInterface(newProduct)
        }

        //Set autocomplete text arrayList
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
        //bindNoPhotoRecyclerView()
        binding.apply {
            itemProduct.setText(newProduct)
            recyclerViewLayout.visibility = View.GONE
            saveAction.setOnClickListener {
                addNewItem()
            }
            shareAction.setIconResource(R.drawable.ic_add_photo_camera)
            shareAction.setText(R.string.camera)
            shareAction.setOnClickListener {
                getCameraInstance()
            }
        }
    }

    private fun setDetailItemInterface() {
        viewModel.currentItem.observe(this.viewLifecycleOwner) { selectedItem ->
            item = selectedItem
            bindItemDetails(item)
            bindPhotoRecyclerView(item)
        }
        setUpdateButtonInterface(false)
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
                shareAction.setText(R.string.filter_cancel)
                shareAction.setIconResource(R.drawable.ic_baseline_cancel_24)
                shareAction.setOnClickListener {
                    setDetailItemInterface()
                    setUpdateButtonInterface(false)
                }
            }
        } else {
            isEditable(false)
            binding.apply {
                saveAction.setText(R.string.edit_action)
                saveAction.setIconResource(R.drawable.ic_edit)
                saveAction.setOnClickListener {
                    setUpdateButtonInterface(true)
                }
                shareAction.setText(R.string.share_action)
                shareAction.setIconResource(R.drawable.ic_send)
                shareAction.setOnClickListener {
                    shareIndexCard(getShareIndexCard(false))
                }
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
            //For reduced Bitmap
            viewModel.uploadPhoto(getReduceBitmapFromCamera(currentPhotoPath))
            //For full size Bitmap
            //viewModel.uploadPhoto(Uri.fromFile(File(currentPhotoPath)))
            //TODO Test performance with different image size
        } else if (requestCode == REQUEST_GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val image = data.data
                if (image != null) {
                    //For reduced Bitmap
                    viewModel.uploadPhoto(getReduceBitmapFromGallery(image))
                    //For full size Bitmap
                    //viewModel.uploadPhoto(image)
                    //TODO Test performance with different image size
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    private fun getReduceBitmapFromCamera(photoPath: String): Uri{
        val fullSizeBitmap = BitmapFactory.decodeFile(photoPath)
        val reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap)
        return Uri.fromFile(createReducedBitmapFile(reducedBitmap))
    }

    private fun getReduceBitmapFromGallery(imageUri: Uri): Uri{
        val fullSizeBitmap = MediaStore.Images.Media.getBitmap(binding.galleryAction.context.contentResolver, imageUri)
        val reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap)
        return Uri.fromFile(createReducedBitmapFile(reducedBitmap))
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
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this.requireContext(),
                            "com.ferpa.fileprovider",
                            it
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
            currentPhotoPath = absolutePath
        }
    }

    private fun createReducedBitmapFile(reducedBitmap: Bitmap): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File.createTempFile("JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */)
        val bos = ByteArrayOutputStream()
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()

        try {
            file.createNewFile()
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            return file
        } catch (e: Exception){
            Log.d("CreateReduceImageFile", "Fail $e" )
        }

        return file
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
            itemPrice.setText(item.getFormattedPrice())
            itemObservations.setText(item.getObservations())
            itemStatus.apply {
                setText(item.status, TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray(R.array.status_options))
                // TODO setOnItemClickListener() "Desea editar la ficha?"
            }
            isEditable(false)
            if (item.currency == "USD") {
                itemPriceLabel.hint = "Precio en USD"
            }

            if (item.photos == "0") {
                recyclerViewLayout.visibility = View.GONE
            }

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

        if (feature3 != "null"){
            if (feature3 != "new") {
                binding.itemFeature3.setText(feature3.toString())
            }
        } else {
            binding.itemFeature3Label.visibility = View.GONE
        }

    }

    private fun bindPhotoRecyclerView(item: Item) {

        if (item.getMachinePhotoList().isEmpty()){
            binding.photoRecyclerView.visibility = View.GONE
        } else {
            val photoAdapter = PhotoAdapter(item.getMachinePhotoList(), this)
            binding.photoRecyclerView.layoutManager =
                LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            binding.photoRecyclerView.adapter = photoAdapter
        }

    }


    private fun bindNoPhotoRecyclerView() {
        val noPhotoAdapter = PhotoAdapter(listOf<MachinePhoto>(MachinePhoto(-1, "")), this)
        binding.photoRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.photoRecyclerView.adapter = noPhotoAdapter
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
            val action =
                AddItemFragmentDirections.actionAddItemFragmentToItemListFragment(newProduct)
            findNavController().navigate(action)
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

    private fun getShareIndexCard(withPrice: Boolean): String {

        var indexCard = resources.getString(
            R.string.index_card,
            item.product,
            item.getFeatures(),
            item.getFormattedPrice()
        )

        if (item.type != null) {

            var indexCard = resources.getString(
                R.string.index_card_with_type,
                item.product,
                item.getFeatures(),
                item.getType(),
                item.getFormattedPrice()
            )
        }

        return indexCard
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

    private fun shareImage(imageUri: String) {

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
        }
        startActivity(Intent.createChooser(intent, null))

    }

    override fun onItemClick(position: Int) {
        val action = AddItemFragmentDirections.actionAddItemFragmentToFullScreenImageFragment(
            position
        )
        this.findNavController().navigate(action)
    }

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
            builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

