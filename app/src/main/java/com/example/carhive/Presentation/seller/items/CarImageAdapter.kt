package com.example.carhive.Presentation.seller.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.carhive.Presentation.seller.view.FullImageDialogFragment
import com.example.carhive.R

class CarImageAdapter(private val imageUrls: List<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = LayoutInflater.from(container.context)
            .inflate(R.layout.item_car_image, container, false) as ImageView

        // Cargar la imagen con Glide
        Glide.with(container.context).load(imageUrls[position]).into(imageView)

        // Al hacer clic en la imagen, abrir el modal con la imagen completa
        imageView.setOnClickListener {
            val fragmentManager = (container.context as FragmentActivity).supportFragmentManager
            val fullImageDialog = FullImageDialogFragment(imageUrls[position])
            fullImageDialog.show(fragmentManager, "fullImageDialog")
        }

        container.addView(imageView)
        return imageView
    }



    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageUrls.size
    }
}
