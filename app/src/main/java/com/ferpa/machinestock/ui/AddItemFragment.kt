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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferpa.machinestock.MachineStockApplication
import com.ferpa.machinestock.ui.viewmodel.MachineStockViewModel

import com.ferpa.machinestock.R
import com.ferpa.machinestock.databinding.FragmentAddItemBinding
import com.ferpa.machinestock.model.*
import com.ferpa.machinestock.ui.adapter.PhotoAdapter
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddItemFragment : Fragment(), PhotoAdapter.OnItemClickListener {

    private val navigationArgs: AddItemFragmentArgs by navArgs()

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MachineStockViewModel by activityViewModels()

    lateinit var currentPhotoPath: String
    private val REQUEST_TAKE_PHOTO = 1

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


        //Get NavigationArgs
        newProduct = navigationArgs.product
        val id = navigationArgs.editId

        //TODO
        if (navigationArgs.product == "TODAS"){
            selectProductDialog(savedInstanceState)
        }


        //Set interface type
        if (id > 0) {
            setDetailItemInterface(id)
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
        bindFeatures(newProduct, null, null, null)
        isEditable(true)
        bindNoPhotoRecyclerView()
        binding.apply {
            itemProduct.setText(newProduct)
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

    private fun setDetailItemInterface(id: Int) {
        viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
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
                    setDetailItemInterface(navigationArgs.editId)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
            bindNewPhotoRecyclerView(currentPhotoPath)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private fun galleryAddPic(file: File) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            this.context?.sendBroadcast(mediaScanIntent)
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

    private fun getGalleryInstance() {
        //TODO Implement getGalleryInstance method
    }

    private fun sharePicture(){
        //TODO Implement sharePicture method
    }

    private fun bindItemDetails(item: Item) {
        binding.apply {
            itemProduct.setText(item.product, TextView.BufferType.SPANNABLE)
            bindFeatures(item.product, item.feature1, item.feature2, item.feature3)
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
            itemStatus.apply {
                setText(item.status, TextView.BufferType.SPANNABLE)
                setAdapter(setAdapterArray(R.array.status_options))
                // TODO setOnItemClickListener() "Desea editar la ficha?"
            }
            isEditable(false)
            if (item.currency == "USD") {
                itemPriceLabel.hint = "Precio en USD"
            }

            if (item.photo1 == null && item.photo2 == null && item.photo3 == null && item.photo4 == null){
                recyclerViewLayout.visibility = View.INVISIBLE
            }


        }
    }

    private fun setAdapterArray(stringArray: Int): ArrayAdapter<String> {
        val array = resources.getStringArray(stringArray)
        return ArrayAdapter(requireContext(), R.layout.dropdown_item, array)
    }

    private fun bindFeatures(product: String, feature1: Double?, feature2: Double?, feature3: String?) {

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
    }

    private fun bindPhotoRecyclerView(item: Item) {
        val photoAdapter = PhotoAdapter(item.getMachinePhotoList(), this)
        binding.photoRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.photoRecyclerView.adapter = photoAdapter
    }

    //TODO implement save photo in web server.

    //Only test method
    private fun bindNewPhotoRecyclerView(currentPath: String){
        var machinePhotoList: MutableList<MachinePhoto> = mutableListOf(
            MachinePhoto(10, currentPath)
        )
        val photoAdapter = PhotoAdapter(machinePhotoList, this)
        binding.photoRecyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.photoRecyclerView.adapter = photoAdapter
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
                "feature3",//TODO Make logic for feature3
                binding.itemPrice.text.toString(),
                binding.itemBrand.text.toString(),
                binding.itemInsideNumber.text.toString(),
                binding.itemLocation.text.toString(),
                "$",
                binding.itemStatus.text.toString(),
                binding.itemOwner2.text.toString(),
                binding.itemOwner1.text.toString()
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
                "feature3",//TODO Make logic for feature3
                binding.itemPrice.text.toString(),
                binding.itemBrand.text.toString(),
                binding.itemInsideNumber.text.toString(),
                binding.itemLocation.text.toString(),
                "$",
                binding.itemStatus.text.toString(),
                binding.itemOwner2.text.toString(),
                binding.itemOwner1.text.toString()
            )
        }

    }

    private fun isEditable(editMode: Boolean) {
        binding.apply {
            itemProduct.isEnabled = false
            itemFeature1.isEnabled = editMode
            itemFeature2.isEnabled = editMode
            itemBrand.isEnabled = editMode
            itemType.isEnabled = editMode
            itemPrice.isEnabled = editMode
            itemInsideNumber.isEnabled = editMode
            itemLocation.isEnabled = editMode
            itemOwner2.isEnabled = editMode
            itemOwner1.isEnabled = editMode
            itemStatus.isEnabled = editMode
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

    private fun shareIndexCard(message: String) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        intent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(intent)
        //TODO el chequeo de whatsapp dejo de funcionar
        if (context?.let { intent.resolveActivity(it.packageManager) } == null) {
            return
        } else {

        }
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

    override fun onItemClick(position: Int) {
        val action = AddItemFragmentDirections.actionAddItemFragmentToFullScreenImageFragment(
            item.id,
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

