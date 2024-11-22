package com.example.carhive.presentation.admin.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.carhive.Presentation.admin.view.FullScreenImageDialogFragment
import com.example.carhive.R
import com.example.carhive.data.datasource.remote.NotificationsRepositoryImpl
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.HistoryEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class CarDetailDialogFragment : DialogFragment() {

    companion object {
        const val CAR_ENTITY_KEY = "car_entity"

        fun newInstance(car: CarEntity): CarDetailDialogFragment {
            val fragment = CarDetailDialogFragment()
            val bundle = Bundle().apply {
                putString("owner_id", car.ownerId)
                putString("car_id", car.id)
                putString("car_model", car.modelo)
                putString("car_brand", car.brand)
                putString("car_price", car.price)
                putString("car_year", car.year)
                putString("car_color", car.color)
                putString("car_mileage", car.mileage)
                putString("car_transmission", car.transmission)
                putString("car_fuelType", car.fuelType)
                putInt("car_doors", car.doors)
                putString("car_engineCapacity", car.engineCapacity)
                putString("car_location", car.location)
                putString("car_condition", car.condition)
                putString("car_vin", car.vin)
                putInt("car_previousOwners", car.previousOwners)
                putInt("car_views", car.views)
                putString("car_listingDate", car.listingDate)
                putString("car_lastUpdated", car.lastUpdated)
                putStringArrayList("car_imageUrls", ArrayList(car.imageUrls ?: emptyList()))
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_car_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            // Set text fields with car details
            view.findViewById<TextView>(R.id.car_model).text = bundle.getString("car_model")
            view.findViewById<TextView>(R.id.car_brand).text = bundle.getString("car_brand")
            view.findViewById<TextView>(R.id.car_price).text = bundle.getString("car_price")
            view.findViewById<TextView>(R.id.car_year).text = bundle.getString("car_year")
            view.findViewById<TextView>(R.id.car_color).text = bundle.getString("car_color")
            view.findViewById<TextView>(R.id.car_mileage).text = bundle.getString("car_mileage")
            view.findViewById<TextView>(R.id.car_transmission).text = bundle.getString("car_transmission")
            view.findViewById<TextView>(R.id.car_fuelType).text = bundle.getString("car_fuelType")
            view.findViewById<TextView>(R.id.car_doors).text = bundle.getInt("car_doors").toString()
            view.findViewById<TextView>(R.id.car_engineCapacity).text = bundle.getString("car_engineCapacity")
            view.findViewById<TextView>(R.id.car_location).text = bundle.getString("car_location")
            view.findViewById<TextView>(R.id.car_condition).text = bundle.getString("car_condition")
            view.findViewById<TextView>(R.id.car_vin).text = bundle.getString("car_vin")
            view.findViewById<TextView>(R.id.car_previousOwners).text = bundle.getInt("car_previousOwners").toString()
            view.findViewById<TextView>(R.id.car_views).text = bundle.getInt("car_views").toString()
            view.findViewById<TextView>(R.id.car_listingDate).text = bundle.getString("car_listingDate")
            view.findViewById<TextView>(R.id.car_lastUpdated).text = bundle.getString("car_lastUpdated")

            // Handle image URLs
            val imageUrls = bundle.getStringArrayList("car_imageUrls")
            if (!imageUrls.isNullOrEmpty()) {
                setupImageCarousel(view, imageUrls)
            }
        }

        // Close button
        val closeButton = view.findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            dismiss()
        }

        // Open Repuve button
        val openWebsiteButton = view.findViewById<Button>(R.id.open_website_button)
        openWebsiteButton.setOnClickListener {
            openRepuveWebsite()
        }

        // Approve button
        view.findViewById<Button>(R.id.approved_button).setOnClickListener {
            val ownerId = arguments?.getString("owner_id") ?: return@setOnClickListener
            val carId = arguments?.getString("car_id") ?: return@setOnClickListener
            val carModel = arguments?.getString("car_model") ?: return@setOnClickListener

            updateCarApprovalStatus(ownerId, carId, carModel, true)
        }

        // Disapprove button
        view.findViewById<Button>(R.id.disapproved_button).setOnClickListener {
            val ownerId = arguments?.getString("owner_id") ?: return@setOnClickListener
            val carId = arguments?.getString("car_id") ?: return@setOnClickListener
            val carModel = arguments?.getString("car_model") ?: return@setOnClickListener

            updateCarApprovalStatus(ownerId, carId, carModel, false)
            sendDisapprovalEmail(ownerId, carId, carModel)
        }
    }

    private fun setupImageCarousel(view: View, imageUrls: ArrayList<String>) {
        val viewPager = view.findViewById<ViewPager2>(R.id.car_image_viewpager)
        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return object : RecyclerView.ViewHolder(imageView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val imageView = holder.itemView as ImageView
                Glide.with(requireContext())
                    .load(imageUrls[position])
                    .into(imageView)

                // Set click listener to open image in full screen
                imageView.setOnClickListener {
                    val fullScreenDialog = FullScreenImageDialogFragment.newInstance(imageUrls, position)
                    fullScreenDialog.show(parentFragmentManager, "FullScreenImageDialog")
                }
            }

            override fun getItemCount(): Int = imageUrls.size
        }
    }

    private fun openRepuveWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www2.repuve.gob.mx:8443/ciudadania/"))
        startActivity(intent)
    }

    private fun updateCarApprovalStatus(ownerId: String, carId: String, carModel: String, isApproved: Boolean) {
        val db = FirebaseDatabase.getInstance().getReference("Car").child(ownerId).child(carId)
        db.child("approved").setValue(isApproved)
            .addOnSuccessListener {
                if (isApproved) {
                    logApprovalHistory(ownerId, carId, carModel)
                    sendApprovalNotification(ownerId, carModel)
                } else {
                    logDisapprovalHistory(ownerId, carId, carModel)
                    sendDisapprovalNotification(ownerId, carModel)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CarDetailDialogFragment", "Error al actualizar el estado de aprobación: ${exception.message}")
            }
    }

    private fun sendDisapprovalEmail(ownerId: String, carId: String, carModel: String) {
        val db = FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
        db.child("email").get().addOnSuccessListener { snapshot ->
            val ownerEmail = snapshot.getValue(String::class.java) ?: return@addOnSuccessListener
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(ownerEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Car Disapproval Notification")
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    Dear user,
                    
                    Your car with ID: $carId and model: $carModel has not been approved. Please review your data and resubmit the listing.
                    
                    Regards,
                    CarHive Team
                    """.trimIndent()
                )
            }
            try {
                startActivity(Intent.createChooser(intent, "Send email"))
            } catch (ex: Exception) {
                Log.e("CarDetailDialogFragment", "Failed to send email: ${ex.message}")
            }
        }
    }

    private fun sendApprovalNotification(ownerId: String, carModel: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = NotificationsRepositoryImpl(requireContext(), FirebaseDatabase.getInstance())
            repository.notifyCarApprovalStatus(
                userId = ownerId,
                carModel = carModel,
                isApproved = true
            )
        }
        dismiss()
    }

    private fun sendDisapprovalNotification(ownerId: String, carModel: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = NotificationsRepositoryImpl(requireContext(), FirebaseDatabase.getInstance())
            repository.notifyCarApprovalStatus(
                userId = ownerId,
                carModel = carModel,
                isApproved = false
            )
        }
        dismiss()
    }

    private fun logApprovalHistory(ownerId: String, carId: String, carModel: String) {
        val timestamp = System.currentTimeMillis()
        val eventType = "Car Approval"
        val message = "The car $carModel has been approved."

        val historyRef = FirebaseDatabase.getInstance().getReference("History/carHistory")
        val historyEntry = HistoryEntity(
            userId = ownerId,
            timestamp = timestamp,
            eventType = eventType,
            message = message
        )
        historyRef.push().setValue(historyEntry)
    }

    private fun logDisapprovalHistory(ownerId: String, carId: String, carModel: String) {
        val timestamp = System.currentTimeMillis()
        val eventType = "Car Disapproval"
        val message = "The car $carModel has been disapproved."

        val historyRef = FirebaseDatabase.getInstance().getReference("History/carHistory")
        val historyEntry = HistoryEntity(
            userId = ownerId,
            timestamp = timestamp,
            eventType = eventType,
            message = message
        )
        historyRef.push().setValue(historyEntry)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
