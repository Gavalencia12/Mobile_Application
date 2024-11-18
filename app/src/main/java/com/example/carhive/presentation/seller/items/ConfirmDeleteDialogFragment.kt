import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.DialogConfirmDeleteBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.graphics.Color // Usa esta importación para el color transparente.

class ConfirmDeleteDialogFragment(private val carId: String, viewModel: CrudViewModel) : DialogFragment() {

    private var _binding: DialogConfirmDeleteBinding? = null
    private val binding get() = _binding!!

    // Get the ViewModel using activityViewModels
    private val viewModel: CrudViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmDeleteBinding.inflate(inflater, container, false) // Inflate the dialog layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Confirm deletion button click listener
        binding.btnConfirm.setOnClickListener {
            // Launch a coroutine to perform the delete operation
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = viewModel.getCurrentUserId() // Get the current user's ID

                // First, delete the car from the database
                viewModel.deleteCar(userId, carId) // Delete the car on confirmation

                // Then, delete the car folder in Firebase Storage
                deleteCarFolderFromStorage(userId, carId)

                // Close the dialog after completing the operation
                dismiss()
            }
        }

        // Cancel button click listener
        binding.btnCancel.setOnClickListener {
            dismiss() // Close the dialog on cancel
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to prevent memory leaks
    }

    // Suspend function to delete the car folder from Firebase Storage
    private suspend fun deleteCarFolderFromStorage(userId: String, carId: String) {
        // Reference to the folder in Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("Car/$userId/$carId/")

        // Get all files inside the folder
        val files = storageRef.listAll().await() // Ensure you have the kotlinx-coroutines-play-services library

        // Delete each file found
        files.items.forEach { file ->
            file.delete().await() // Delete each file in the folder
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}
