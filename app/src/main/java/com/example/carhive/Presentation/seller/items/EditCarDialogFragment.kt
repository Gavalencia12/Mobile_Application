package com.example.carhive.Presentation.seller.view

import android.app.Dialog
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
import kotlinx.coroutines.launch

class EditCarDialogFragment : DialogFragment() {

    private var _binding: DialogCarOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CrudViewModel
    private lateinit var car: CarEntity
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (selectedImages.size < 5) {
                    selectedImages.add(uri)
                    selectedImagesAdapter.notifyDataSetChanged()
                    updateImageCounter()
                } else {
                    Toast.makeText(requireContext(), "You can only select up to 5 images", Toast.LENGTH_SHORT).show()
                }
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
        binding.dialogTitle.text = "Edit Car"
        binding.buttonCreate.text = "Save Changes"

        binding.etModelo.setText(car.modelo)
        binding.etColor.setText(car.color)
        binding.etSpeed.setText(car.speed)
        binding.etAddOn.setText(car.addOn)
        binding.etDescription.setText(car.description)
        binding.etPrice.setText(car.price)

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
        binding.tvImageCount.text = "${selectedImages.size}/5 images selected"
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
            viewModel.viewModelScope.launch {
                val userId = viewModel.getCurrentUserId()
                viewModel.updateCar(
                    userId = userId,
                    carId = car.id,
                    modelo = modelo,
                    color = color,
                    speed = speed,
                    addOn = addOn,
                    description = description,
                    price = price,
                    images = selectedImages
                )
                dismiss()
            }
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}