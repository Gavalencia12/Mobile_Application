package com.example.carhive.Presentation.admin.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.R
import com.google.firebase.database.FirebaseDatabase

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

            val imageUrls = bundle.getStringArrayList("car_imageUrls")
            if (!imageUrls.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrls[0])
                    .into(view.findViewById(R.id.car_image))
            }
        }
        val closeButton = view.findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.open_website_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www2.repuve.gob.mx:8443/ciudadania/"))
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.approved_button).setOnClickListener {
            updateCarApprovalStatus(true)
            dismiss()
        }

        view.findViewById<Button>(R.id.desapproved_button).setOnClickListener {
            val ownerId = arguments?.getString("owner_id")
            val carId = arguments?.getString("car_id")

            if (ownerId != null) {
                val db = FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
                // Obtener el correo electrónico, firstName y lastName del propietario
                db.child("email").get().addOnSuccessListener { emailSnapshot ->
                    val ownerEmail = emailSnapshot.value as? String
                    db.child("firstName").get().addOnSuccessListener { firstNameSnapshot ->
                        val firstName = firstNameSnapshot.value as? String
                        db.child("lastName").get().addOnSuccessListener { lastNameSnapshot ->
                            val lastName = lastNameSnapshot.value as? String

                            if (ownerEmail != null && firstName != null && lastName != null) {
                                val fullName = "$firstName $lastName"
                                sendEmail(ownerEmail, carId, fullName)
                            } else {
                                Log.e("CarDetailDialogFragment", "No se encontró el email o nombre completo para el ownerId: $ownerId")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("CarDetailDialogFragment", "Error al obtener el apellido del propietario: ", exception)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("CarDetailDialogFragment", "Error al obtener el nombre del propietario: ", exception)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("CarDetailDialogFragment", "Error al obtener el correo del propietario: ", exception)
                }
            }
            updateCarApprovalStatus(false)
        }


    }

    private fun sendEmail(ownerEmail: String, carId: String?, fullName: String) {
        if (!isAdded || !isVisible) {
            Log.e("CarDetailDialogFragment", "El fragmento no está adjunto o visible en la actividad")
            return
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Solo apps de email deben manejar este Intent
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ownerEmail))
            putExtra(Intent.EXTRA_SUBJECT, "Notificación de desaprobación de su coche")
            putExtra(
                Intent.EXTRA_TEXT,
                """
            Estimado usuario $fullName, 
            
            Su coche con ID: $carId no ha sido aprobado. Por favor, revise sus datos y vuelva a intentarlo.
            
            Sus datos a revisar son:
            -
            
            Saludos,
            El equipo de CarHive
            """.trimIndent()
            )
        }
        try {
            startActivity(Intent.createChooser(intent, "Enviar correo"))
        } catch (ex: Exception) {
            Log.e("CarDetailDialogFragment", "No se pudo enviar el correo electrónico.", ex)
        }
    }




    private fun updateCarApprovalStatus(isApproved: Boolean) {
        val ownerId = arguments?.getString("owner_id")
        val carId = arguments?.getString("car_id")

        if (ownerId == null || carId == null) {
            return
        }

        val db = FirebaseDatabase.getInstance().getReference("Car").child(ownerId).child(carId)

        db.child("approved").setValue(isApproved)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
            }
    }




    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 1).toInt(),
            (resources.displayMetrics.heightPixels * 1).toInt()
        )
    }

}
