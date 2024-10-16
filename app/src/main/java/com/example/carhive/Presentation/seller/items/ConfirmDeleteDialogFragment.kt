import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.DialogConfirmDeleteBinding
import kotlinx.coroutines.launch

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
                viewModel.deleteCar(userId, carId) // Eliminar el coche al confirmar
            }
            dismiss() // Cerrar el diálogo
        }

        binding.btnCancel.setOnClickListener {
            dismiss() // Cerrar el diálogo al cancelar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
