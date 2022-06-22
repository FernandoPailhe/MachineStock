package com.ferpa.machinestock.utilities.imageUtils


import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageManager {

    companion object{

        fun getReduceBitmapFromCamera(photoPath: String, orientation: Int, context: Context): Uri {
            val fullSizeBitmap = BitmapFactory.decodeFile(photoPath)
            var reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap)
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                reducedBitmap = ImageRotator.rotateImage(reducedBitmap, 90.0f)
            }
            return Uri.fromFile(createBitmapFile(reducedBitmap, context))
        }

        fun getReduceBitmapFromGallery(imageUri: Uri, context: Context): Uri {
            val fullSizeBitmap = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                imageUri
            )
            val reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap)
            return Uri.fromFile(createBitmapFile(reducedBitmap, context))
        }

        private fun createBitmapFile(reducedBitmap: Bitmap, context: Context): File {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val file = File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
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
            } catch (e: Exception) {
                Log.d("CreateReduceImageFile", "Fail $e")
            }

            return file
        }

    }
}