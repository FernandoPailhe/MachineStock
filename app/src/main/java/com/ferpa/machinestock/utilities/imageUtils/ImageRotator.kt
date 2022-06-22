package com.ferpa.machinestock.utilities.imageUtils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Credits to Sami Eltamawy and Shirish Herwade -> https://stackoverflow.com/a/31720143
 */
class ImageRotator {

    companion object {

        fun prepareAndStoreImageFile(imageFile: File, destinationFile: File): File? {
            return try {
                val processedBmp = handleBitmapDownsamplingAndRotation(imageFile)
                destinationFile.outputStream().buffered().use { bufferedOutputStream ->
                    processedBmp?.compress(Bitmap.CompressFormat.PNG, 85, bufferedOutputStream)
                }
                destinationFile
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                null
            } catch (se: SecurityException) {
                se.printStackTrace()
                null
            }
        }


        /**
         * This method is responsible for solving the rotation issue if exist. Also scale the images to
         * 2048x2048 resolution
         *
         * @throws IOException
         * @throws SecurityException
         */
        @Throws(IOException::class, SecurityException::class)
        fun handleBitmapDownsamplingAndRotation(imageFile: File): Bitmap? {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            FileInputStream(imageFile).use { imageStream ->
                BitmapFactory.decodeStream(imageStream, null, options)
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            val bitmap = FileInputStream(imageFile).use { imageStream ->
                BitmapFactory.decodeStream(imageStream, null, options)
            }

            return bitmap?.let { rotateImageIfRequired(it, imageFile) }
        }

        /**
         * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
         * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
         * the closest inSampleSize that will result in the final decoded bitmap having a width and
         * height equal to or larger than the requested width and height. This implementation does not
         * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
         * results in a larger bitmap which isn't as useful for caching purposes.
         *
         * @param options   An options object with out* params already populated (run through a decode*
         * method with inJustDecodeBounds==true
         * @param reqWidth  The requested width of the resulting bitmap
         * @param reqHeight The requested height of the resulting bitmap
         * @return The value to be used for inSampleSize
         */
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth

            return if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

                // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
                // with both dimensions larger than or equal to the requested height and width.
                val inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

                // This offers some additional logic in case the image has a strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might still
                // end up being too large to fit comfortably in memory, so we should
                // be more aggressive with sample down the image (=larger inSampleSize).

                val totalPixels = (width * height).toFloat()

                // Anything more than 2x the requested pixels we'll sample down further
                val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

                calculateInSampleSize(totalPixels, inSampleSize, totalReqPixelsCap)
            } else 1
        }

        private tailrec fun calculateInSampleSize(
            totalPixels: Float,
            inSampleSize: Int,
            totalRequiredPixelsCap: Float
        ): Int {
            val tooBig = totalRequiredPixelsCap < totalPixels / (inSampleSize * inSampleSize)
            return if (tooBig) calculateInSampleSize(
                totalPixels,
                inSampleSize + 1,
                totalRequiredPixelsCap
            ) else inSampleSize
        }

        @Throws(IOException::class, SecurityException::class)
        private fun rotateImageIfRequired(img: Bitmap, imageFile: File): Bitmap {
            val ei = FileInputStream(imageFile).use { ExifInterface(it) }
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }
        }

        fun rotateImage(img: Bitmap, degree: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degree)
            val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
            img.recycle()
            return rotatedImg
        }

        const val MAX_HEIGHT = 2048
        const val MAX_WIDTH = 2048
    }
}