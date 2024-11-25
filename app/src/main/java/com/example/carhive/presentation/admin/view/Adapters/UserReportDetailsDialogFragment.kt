package com.example.carhive.presentation.admin.view.Adapters

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.carhive.R
import com.example.carhive.data.model.UserReport
import com.example.carhive.databinding.DialogUserReportDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserReportDetailsDialogFragment : DialogFragment() {

    private var _binding: DialogUserReportDetailsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_REPORT = "user_report"

        fun newInstance(userReport: UserReport): UserReportDetailsDialogFragment {
            val fragment = UserReportDetailsDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_REPORT, userReport)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userReport: UserReport = requireArguments().getParcelable(ARG_REPORT)!!

        // Llenar el comentario del usuario
        binding.messageComment.text = userReport.comment

        // Agregar los mensajes al contenedor dinámicamente
        val messagesContainer = binding.messagesContainer
        messagesContainer.removeAllViews() // Limpia los mensajes previos

        userReport.sampleMessages.forEach { message ->
            // Inflar el diseño del contenido del mensaje
            val contentView = LayoutInflater.from(requireContext())
                .inflate(R.layout.message_item, messagesContainer, false) as TextView
            contentView.text = "${message.content}"

            val timeView = LayoutInflater.from(requireContext())
                .inflate(R.layout.message_time, messagesContainer, false) as TextView
            val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))
            timeView.text = formattedTime


            // Agregar los TextViews inflados al contenedor
            messagesContainer.addView(contentView)
            messagesContainer.addView(timeView)
        }

        // Formatear y mostrar la fecha del reporte
        binding.textViewTimestamp.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(userReport.timestamp))

        // Configuración para cerrar el diálogo
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog ?: return
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% del ancho de la pantalla
        val height = ViewGroup.LayoutParams.WRAP_CONTENT // Ajusta la altura automáticamente
        dialog.window?.setLayout(width, height)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
