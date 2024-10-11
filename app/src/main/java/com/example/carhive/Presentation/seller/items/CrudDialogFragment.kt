package com.example.carhive.Presentation.seller.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding

class CrudDialogFragment : DialogFragment() {

    private val viewModel: CrudViewModel by activityViewModels()
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val selectedImages = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el lanzador de la actividad para seleccionar imágenes
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    // Permite seleccionar múltiples imágenes
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        selectedImages.add(imageUri)
                        // Aquí puedes mostrar las imágenes seleccionadas en el contenedor (opcional)
                    }
                } else {
                    // Si solo se selecciona una imagen
                    result.data?.data?.let { uri ->
                        selectedImages.add(uri)
                        // Aquí puedes mostrar la imagen seleccionada en el contenedor (opcional)
                    }
                }
            }
        }

        // Botón para seleccionar imágenes
        binding.buttonSelectImages.setOnClickListener {
            openImagePicker()
        }

        // Obtener los datos introducidos en los campos de texto
        binding.buttonCreate.setOnClickListener {
            val modelo = binding.etModelo.text.toString()
            val color = binding.etColor.text.toString()
            val speed = binding.etSpeed.text.toString()
            val addOn = binding.etAddOn.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()

            // Aquí puedes usar los valores obtenidos para crear el coche
            viewModel.addCarToDatabase(
                modelo = modelo,
                color = color,
                speed = speed,
                addOn = addOn,
                description = description,
                price = price,
                images = selectedImages
            )

            dismiss() // Cierra el diálogo
        }

        binding.buttonCancel.setOnClickListener {
            dismiss() // Cierra el diálogo
        }
    }

    // Función para abrir el selector de imágenes
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Permitir seleccionar múltiples imágenes
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_Dialog // Puedes personalizar el tema si es necesario
    }
}
