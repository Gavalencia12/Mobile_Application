package com.example.carhive.Presentation.seller.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.R
import com.example.carhive.databinding.DialogCarOptionsBinding
import com.example.carhive.databinding.ItemSelectedImageBinding

class CrudDialogFragment : DialogFragment() {

    private val viewModel: CrudViewModel by activityViewModels()
    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val maxImages = 5 // Limite de imágenes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCarOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el RecyclerView y el adaptador para mostrar las imágenes seleccionadas
        selectedImagesAdapter = SelectedImagesAdapter(selectedImages) { position ->
            removeImage(position)
        }

        binding.rvSelectedImages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }

        // Inicializa el lanzador de la actividad para seleccionar imágenes
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                if (clipData != null) {
                    // Permite seleccionar múltiples imágenes, pero con un límite
                    val newImagesCount = clipData.itemCount
                    if (selectedImages.size + newImagesCount <= maxImages) {
                        for (i in 0 until newImagesCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            selectedImages.add(imageUri)
                        }
                    } else {
                        showMaxImagesError()
                    }
                } else {
                    // Si solo se selecciona una imagen
                    result.data?.data?.let { uri ->
                        if (selectedImages.size < maxImages) {
                            selectedImages.add(uri)
                        } else {
                            showMaxImagesError()
                        }
                    }
                }
                selectedImagesAdapter.notifyDataSetChanged()
                updateImageCounter() // Actualiza el contador
            }
        }

        // Botón para seleccionar imágenes
        binding.buttonSelectImages.setOnClickListener {
            if (selectedImages.size < maxImages) {
                openImagePicker()
            } else {
                showMaxImagesError()
            }
        }

        // Crear el coche con las imágenes seleccionadas
        binding.buttonCreate.setOnClickListener {
            val modelo = binding.etModelo.text.toString()
            val color = binding.etColor.text.toString()
            val speed = binding.etSpeed.text.toString()
            val addOn = binding.etAddOn.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()

            // Verificar que todos los campos estén llenos
            if (modelo.isEmpty() || color.isEmpty() || speed.isEmpty() || addOn.isEmpty() ||
                description.isEmpty() || price.isEmpty() || selectedImages.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
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
        }

        binding.buttonCancel.setOnClickListener {
            dismiss() // Cierra el diálogo
        }

        // Actualiza el contador de imágenes al iniciar
        updateImageCounter()
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

    // Función para eliminar una imagen seleccionada
    private fun removeImage(position: Int) {
        selectedImages.removeAt(position)
        selectedImagesAdapter.notifyItemRemoved(position)
        updateImageCounter() // Actualiza el contador
    }

    // Mostrar error si se excede el número máximo de imágenes
    private fun showMaxImagesError() {
        Toast.makeText(requireContext(), "Solo puedes seleccionar un máximo de $maxImages imágenes", Toast.LENGTH_SHORT).show()
    }

    // Función para actualizar el contador de imágenes seleccionadas
    private fun updateImageCounter() {
        binding.tvImageCount.text = "${selectedImages.size}/$maxImages images selected"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_Dialog // Puedes personalizar el tema si es necesario
    }
}

class SelectedImagesAdapter(
    private val images: List<Uri>,
    private val onRemoveImage: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedImageView: ImageView = itemView.findViewById(R.id.iv_selected_image)
        val removeImageButton: ImageButton = itemView.findViewById(R.id.btn_remove_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position]

        if (imageUri.scheme == "content") {
            // Cargar desde Uri local (imágenes nuevas)
            Glide.with(holder.itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.ic_img)  // Imagen por defecto mientras carga
                .error(R.drawable.ic_error)      // Imagen si hay error
                .into(holder.selectedImageView)
        } else {
            // Cargar desde una URL (imágenes existentes de Firebase)
            Glide.with(holder.itemView.context)
                .load(imageUri.toString())  // Convertir Uri a String para Glide
                .placeholder(R.drawable.ic_img)  // Imagen por defecto mientras carga
                .error(R.drawable.ic_error)      // Imagen si hay error
                .into(holder.selectedImageView)
        }

        // Eliminar imagen
        holder.removeImageButton.setOnClickListener {
            onRemoveImage(position)
        }
    }


    override fun getItemCount(): Int {
        return images.size
    }
}
