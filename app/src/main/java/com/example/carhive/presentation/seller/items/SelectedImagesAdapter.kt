import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R

class SelectedImagesAdapter(
    private val images: MutableList<Uri>, // List of selected image URIs
    private val onRemoveImage: (Int) -> Unit // Callback for removing an image
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    // ViewHolder to hold image and remove button
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedImageView: ImageView = itemView.findViewById(R.id.iv_selected_image) // ImageView for displaying the image
        val removeImageButton: ImageButton = itemView.findViewById(R.id.btn_remove_image) // Button to remove the image
    }

    // Inflate the view for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Inflate item layout for the selected image
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view) // Return the ViewHolder with the inflated view
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position] // Get the URI of the current image
        Glide.with(holder.itemView.context)
            .load(imageUri) // Load the image using Glide
            .placeholder(R.drawable.ic_img) // Placeholder while loading
            .error(R.drawable.ic_error) // Error image if loading fails
            .into(holder.selectedImageView) // Load the image into the ImageView

        // Set listener for the remove image button
        holder.removeImageButton.setOnClickListener {
            onRemoveImage(position) // Call the callback to remove the image at the given position
        }
    }

    // Return the total number of items in the adapter
    override fun getItemCount(): Int {
        return images.size // Return the total number of images
    }

}
