package com.example.carhive.presentation.user.items

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.AdditionalDataEntity
import com.example.carhive.databinding.FragmentUpdateDataDialogBinding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.fragment.app.setFragmentResult

class UpdateDataDialogFragment : DialogFragment() {

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var _binding: FragmentUpdateDataDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.ThemeOverlay_Material_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateDataDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val marginDp = 20
        val marginPx = (marginDp * resources.displayMetrics.density).toInt()

        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels

        dialog?.window?.apply {
            setLayout(width - 2 * marginPx, height - 5 * marginPx)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.decorView?.let { decorView ->
            decorView.clipToOutline = true
            decorView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val radius = 32f
                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
        }
        binding.root.background = MaterialShapeDrawable(
            ShapeAppearanceModel.builder()
                .setAllCornerSizes(32f)
                .build()
        ).apply {
            fillColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
        }

        binding.btnSelectImage.setOnClickListener {
            openImageChooser()
        }


        Glide.with(requireContext())
            .load("url_de_imagen_actual")
            .circleCrop()
            .into(binding.ivUserImage)

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                showLoadingDialog() // Mostrar ProgressDialog
                saveDataToFirebaseAsync()
                hideLoadingDialog() // Ocultar ProgressDialog
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(requireContext())
                .load(imageUri)
                .circleCrop()
                .into(binding.ivUserImage)
            binding.ivUserImage.visibility = View.VISIBLE
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Saving data, please wait...")
            setCancelable(false) // Evita que se pueda cancelar
            show()
        }
    }

    private fun hideLoadingDialog() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private suspend fun saveDataToFirebaseAsync() {
        val year = binding.etYear.text.toString().toIntOrNull()
        val address = binding.etAddress.text.toString()
        val interiorNumber = binding.etInteriorNumber.text.toString()
        val outerNumber = binding.etOuterNumber.text.toString()
        val sex = when (binding.rgSex.checkedRadioButtonId) {
            binding.rbMale.id -> "Male"
            binding.rbFemale.id -> "Female"
            else -> null
        }
        val description = binding.etDescription.text.toString()
        val phoneNumber = binding.etPhone.text.toString()

        // Validar campos requeridos
        if (year == null || address.isEmpty() || outerNumber.isEmpty() || sex == null || phoneNumber.isEmpty()) {
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                // Mostrar error al usuario
                showInstructionError(
                    "Please fill in all required fields.",
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
            }
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Subir imagen si existe
        val imageUrl = if (imageUri != null) {
            uploadImageToFirebaseAsync(imageUri!!)
        } else {
            null
        }

        // Datos adicionales
        val additionalData = AdditionalDataEntity(
            userId = userId,
            year = year,
            address = address,
            interiorNumber = interiorNumber,
            outerNumber = outerNumber,
            sex = sex,
            description = description
        )

        val userUpdates = mutableMapOf<String, Any?>(
            "phoneNumber" to phoneNumber,
            "imageUrl" to imageUrl
        )

        try {
            withContext(Dispatchers.IO) {
                val database = FirebaseDatabase.getInstance().reference
                database.child("Users").child(userId).updateChildren(userUpdates).await()
                database.child("additionalData").child(userId).setValue(additionalData).await()
            }

            withContext(Dispatchers.Main) {
                setFragmentResult("update_request_key", Bundle())
                dismiss()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                hideLoadingDialog()
                // Manejar errores (mostrar mensaje al usuario)
                showInstructionError(
                    "Error saving data. Please try again.",
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
            }
        }
    }

    private fun showInstructionError(message: String, color: Int) {
        binding.instruction.apply {
            text = message
            setTextColor(color)
            visibility = View.VISIBLE
            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_error, // Left drawable for error icon
                0,
                0,
                0
            )
            compoundDrawablePadding = 3 // Space between icon and text
        }
    }

    private suspend fun uploadImageToFirebaseAsync(uri: Uri): String? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val storageReference = FirebaseStorage.getInstance().reference.child("Users/$userId/profile.jpg")

        return try {
            val uploadTask = storageReference.putFile(uri).await()
            storageReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
