package com.decode.samplestickerapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageSelection : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private lateinit var imgSelectedImage: ImageView
    private val MAX_FILE_SIZE = 50 * 1024 // 50KB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)

        val btnPickImage = findViewById<Button>(R.id.btnPickImage)
        val btnViewSavedImages = findViewById<Button>(R.id.btnViewSavedImages)
        imgSelectedImage = findViewById(R.id.imgSelectedImage)

        // Set a click listener on the button to pick an image
        btnPickImage.setOnClickListener {
            pickImageFromGallery()
        }

        // Navigate to ImageListActivity
        btnViewSavedImages.setOnClickListener {
            val intent = Intent(this, SavedImagesActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to pick an image from the gallery
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handling the result of the picked image or edited image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data!!
                startImageCrop(imageUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                imgSelectedImage.setImageURI(resultUri) // Display the edited image
                saveImageWithSizeLimit(resultUri) // Save image under 50KB
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            // Handle crop error here
            Toast.makeText(this, "Image Crop Error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Start the image cropping activity
    private fun startImageCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "edited_image.png"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)  // Aspect ratio for stickers
            .withMaxResultSize(512, 512)
            .start(this)
    }

    // Save the edited image with size less than 50KB
    private fun saveImageWithSizeLimit(imageUri: Uri) {
        val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

        // Save the image to the app's stickers directory
        val stickersDirectory = File(filesDir, "stickers")
        if (!stickersDirectory.exists()) {
            stickersDirectory.mkdir()
        }

        val stickerFile = File(stickersDirectory, "Sticker_${System.currentTimeMillis()}.png")
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(stickerFile)

            // Start with max quality
            var quality = 100
            var byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream)

            // Reduce quality until the image size is under 50KB
            while (byteArrayOutputStream.toByteArray().size > MAX_FILE_SIZE && quality > 0) {
                byteArrayOutputStream.reset()
                quality -= 5 // Decrease quality by 5
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream)
            }

            // Write compressed image to file
            outputStream.write(byteArrayOutputStream.toByteArray())
            outputStream.flush()

            Toast.makeText(this, "Sticker saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving sticker: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            outputStream?.close()
        }
    }
}
