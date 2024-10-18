package com.example.carhive.Presentation.seller.view

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditCarDialogFragment : DialogFragment() {

    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CrudViewModel
    private lateinit var car: CarEntity
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private val existingImageUrls = mutableListOf<String>()

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.let { data ->
                    // Manejar múltiples imágenes seleccionadas
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri)
                        }
                    } else {
                        data.data?.let { uri ->
                            requireContext().contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            selectedImages.add(uri)
                        }
                    }
                    selectedImagesAdapter.notifyDataSetChanged()
                    updateImageCounter()
                }
            }
        }


    companion object {
        fun newInstance(car: CarEntity, viewModel: CrudViewModel): EditCarDialogFragment {
            val fragment = EditCarDialogFragment()
            fragment.car = car
            fragment.viewModel = viewModel
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTheme_FullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Cargar los datos actuales del coche
        binding.etModelo.setText(car.modelo)
        binding.etColor.setText(car.color)
        binding.etSpeed.setText(car.speed)
        binding.etAddOn.setText(car.addOn)
        binding.etDescription.setText(car.description)
        binding.etPrice.setText(car.price)

        // Añade las URLs de las imágenes existentes a la lista de imágenes seleccionadas
        car.imageUrls?.forEach { imageUrl ->
            existingImageUrls.add(imageUrl)
            selectedImages.add(Uri.parse(imageUrl)) // Añadir a la lista de URIs para el adaptador
        }

        // Configura el RecyclerView para mostrar las imágenes seleccionadas
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            selectedImagesAdapter.notifyItemRemoved(position)
            updateImageCounter()
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }

        updateImageCounter()
    }

    private fun setupListeners() {
        binding.buttonSelectImages.setOnClickListener {
            openImagePicker()
        }

        binding.buttonCreate.setOnClickListener {
            updateCar()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun updateImageCounter() {
        val maxImages = 5
        binding.tvImageCount.text = "${selectedImages.size} / $maxImages images selected"
    }


    private fun updateCar() {
        val modelo = binding.etModelo.text.toString()
        val color = binding.etColor.text.toString()
        val speed = binding.etSpeed.text.toString()
        val addOn = binding.etAddOn.text.toString()
        val description = binding.etDescription.text.toString()
        val price = binding.etPrice.text.toString()

        if (modelo.isNotEmpty() && color.isNotEmpty() && speed.isNotEmpty() && addOn.isNotEmpty() &&
            description.isNotEmpty() && price.isNotEmpty()) {

            val newImages = selectedImages.filter { uri -> !existingImageUrls.contains(uri.toString()) }
            val imagesToKeep = selectedImages.filter { uri -> existingImageUrls.contains(uri.toString()) }

            // Mostrar el ProgressDialog o ProgressBar
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Subiendo imágenes, por favor espera 3 segundos más...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            viewModel.viewModelScope.launch {
                try {
                    val userId = viewModel.getCurrentUserId()

                    // Subir imágenes nuevas a Firebase Storage
                    val uploadedImageUrls = mutableListOf<String>()
                    newImages.forEach { uri ->
                        val imageRef = FirebaseStorage.getInstance().reference.child("cars/${uri.lastPathSegment}")
                        val uploadTask = imageRef.putFile(uri)
                        val downloadUrl = uploadTask.await().storage.downloadUrl.await()
                        uploadedImageUrls.add(downloadUrl.toString())
                    }

                    viewModel.updateCar(
                        userId = userId,
                        carId = car.id,
                        modelo = modelo,
                        color = color,
                        speed = speed,
                        addOn = addOn,
                        description = description,
                        price = price,
                        newImages = newImages,
                        existingImages = imagesToKeep
                    )

                    // Cerrar ProgressDialog y mostrar mensaje de éxito
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Los cambios se han guardado exitosamente.", Toast.LENGTH_SHORT).show()
                    dismiss()

                } catch (e: Exception) {
                    // Si ocurre un error, cerrar ProgressDialog y mostrar mensaje de error
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Error al subir imágenes o guardar cambios. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
