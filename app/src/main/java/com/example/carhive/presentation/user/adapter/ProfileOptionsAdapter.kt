package com.example.carhive.presentation.user.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.carhive.R

class ProfileOptionsAdapter(
    private val context: Context,
    private val options: List<String>,
    private val icons: List<Int>
) : ArrayAdapter<String>(context, 0, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_profile_option, parent, false)

        val optionTitle = view.findViewById<TextView>(R.id.option_title)
        val optionIcon = view.findViewById<ImageView>(R.id.option_icon)

        optionTitle.text = options[position]
        optionIcon.setImageResource(icons[position])

        return view
    }
}
