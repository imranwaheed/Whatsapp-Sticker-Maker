package com.decode.samplestickerapp

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageAdapter(
    private val context: Context,
    private var imageFiles: MutableList<File>,
    private val onImageDeleted: (File) -> Unit // Callback to notify activity when an image is deleted
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            // Set a long press listener for each image
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val imageFile = imageFiles[position]
                    showDeleteConfirmationDialog(imageFile, position)
                }
                true
            }
        }

        private fun showDeleteConfirmationDialog(file: File, position: Int) {
            // Show an AlertDialog to confirm the deletion
            AlertDialog.Builder(context).apply {
                setTitle("Delete Image")
                setMessage("Are you sure you want to delete this image?")
                setPositiveButton("Delete") { _, _ ->
                    // Delete the image file
                    if (file.delete()) {
                        // Notify the activity to remove the item from the list
                        onImageDeleted(file)
                        // Remove the file from the list and notify the adapter
                        imageFiles.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                setNegativeButton("Cancel", null)
                show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageFile = imageFiles[position]
        val uri = Uri.fromFile(imageFile)
        holder.imageView.setImageURI(uri)
    }

    override fun getItemCount(): Int {
        return imageFiles.size
    }

    // Update the adapter's list if images are modified
    fun updateImages(newImageFiles: List<File>) {
        this.imageFiles = newImageFiles.toMutableList()
        notifyDataSetChanged()
    }
}
