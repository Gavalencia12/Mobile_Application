import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.DialogConfirmDeleteBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConfirmDeleteDialogFragment(private val carId: String, viewModel: CrudViewModel) : DialogFragment() {

    private var _binding: DialogConfirmDeleteBinding? = null
    private val binding get() = _binding!!

    // Obtén el ViewModel usando activityViewModels
    private val viewModel: CrudViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            // Llama a la función suspendida dentro de una corrutina
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = viewModel.getCurrentUserId() // Obtén el ID del usuario actual

                // Primero, eliminamos el coche de la base de datos
                viewModel.deleteCar(userId, carId) // Eliminar el coche al confirmar

                // Luego, eliminamos la carpeta del coche en Firebase Storage
                deleteCarFolderFromStorage(userId, carId)

                // Cierra el diálogo después de completar la operación
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss() // Cerrar el diálogo al cancelar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun deleteCarFolderFromStorage(userId: String, carId: String) {
        // Referencia a la carpeta en Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("Car/$userId/$carId/")

        // Obtén todos los archivos dentro de la carpeta
        val files = storageRef.listAll().await() // Asegúrate de tener la librería kotlinx-coroutines-play-services

        // Elimina cada archivo encontrado
        files.items.forEach { file ->
            file.delete().await() // Eliminar cada archivo de la carpeta
        }
    }
}
