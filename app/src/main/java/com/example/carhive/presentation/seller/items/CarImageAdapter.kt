package com.example.carhive.presentation.seller.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.carhive.presentation.seller.view.FullImageDialogFragment
import com.example.carhive.R

class CarImageAdapter(private val imageUrls: List<String>) : PagerAdapter() {

    // This method determines if a view is associated with a specific key object
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    // This method is responsible for creating the item for the ViewPager
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // Inflate the image view from the layout
        val imageView = LayoutInflater.from(container.context)
            .inflate(R.layout.item_car_image, container, false) as ImageView

        // Load the image using Glide from the provided URL
        Glide.with(container.context).load(imageUrls[position]).into(imageView)

        // Set a click listener to open a dialog with the full image when the image is clicked
        imageView.setOnClickListener {
            val fragmentManager = (container.context as FragmentActivity).supportFragmentManager
            val fullImageDialog = FullImageDialogFragment(imageUrls[position])
            fullImageDialog.show(fragmentManager, "fullImageDialog") // Show the full image dialog
        }

        // Add the imageView to the container
        container.addView(imageView)
        return imageView // Return the created view
    }

    // This method removes the item from the ViewPager
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View) // Remove the view from the container
    }

    // This method returns the total number of images in the adapter
    override fun getCount(): Int {
        return imageUrls.size
    }
}
