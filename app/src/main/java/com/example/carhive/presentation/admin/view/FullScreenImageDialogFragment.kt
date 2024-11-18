package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.carhive.R

class FullScreenImageDialogFragment : DialogFragment() {

    companion object {
        private const val IMAGE_URLS_KEY = "image_urls"
        private const val START_POSITION_KEY = "start_position"

        fun newInstance(imageUrls: List<String>, startPosition: Int = 0): FullScreenImageDialogFragment {
            val fragment = FullScreenImageDialogFragment()
            val args = Bundle()
            args.putStringArrayList(IMAGE_URLS_KEY, ArrayList(imageUrls))
            args.putInt(START_POSITION_KEY, startPosition)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_full_screen_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrls = arguments?.getStringArrayList(IMAGE_URLS_KEY) ?: emptyList()
        val startPosition = arguments?.getInt(START_POSITION_KEY) ?: 0

        val viewPager = view.findViewById<ViewPager2>(R.id.full_screen_view_pager)
        viewPager.adapter = ImagePagerAdapter(imageUrls)
        viewPager.setCurrentItem(startPosition, false)
    }

    inner class ImagePagerAdapter(private val images: List<String>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.full_screen_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_full_screen_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            Glide.with(this@FullScreenImageDialogFragment)
                .load(images[position])
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                dismiss()
            }
        }

        override fun getItemCount(): Int = images.size
    }
}
