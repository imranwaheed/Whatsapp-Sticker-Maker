package com.decode.samplestickerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SavedImagesActivity : AppCompatActivity() {

    private lateinit var adapter: ImageAdapter
    private lateinit var imageFiles: MutableList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_images)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load the images from the internal "stickers" directory
        val stickersDirectory = File(filesDir, "stickers")
        imageFiles = stickersDirectory.listFiles()?.filter { it.isFile && it.extension == "png" }?.toMutableList() ?: mutableListOf()

        // Set the adapter to the RecyclerView
        adapter = ImageAdapter(this, imageFiles) { deletedFile ->
            // Handle the image deletion callback
            imageFiles.remove(deletedFile)
            adapter.updateImages(imageFiles)
        }
        recyclerView.adapter = adapter
    }
}
