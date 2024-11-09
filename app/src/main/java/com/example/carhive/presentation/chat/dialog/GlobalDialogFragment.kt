package com.example.carhive.presentation.chat.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.carhive.databinding.DialogGlobalBinding
import com.example.carhive.presentation.chat.viewModel.ChatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GlobalDialogFragment(
    private val onActionCompleted: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: DialogGlobalBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogGlobalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Extrae los argumentos
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val message = arguments?.getString(ARG_MESSAGE) ?: ""
        val showCheckBox = arguments?.getBoolean(ARG_SHOW_CHECKBOX) ?: false
        val positiveButtonText = arguments?.getString(ARG_POSITIVE_BUTTON_TEXT) ?: "Aceptar"
        val negativeButtonText = arguments?.getString(ARG_NEGATIVE_BUTTON_TEXT) ?: "Cancelar"
        val dialogType = arguments?.getSerializable(ARG_DIALOG_TYPE) as? DialogType

        binding.dialogTitle.text = title
        binding.dialogMessage.text = message
        binding.checkboxBlockChat.visibility = if (showCheckBox) View.VISIBLE else View.GONE
        binding.buttonPositive.text = positiveButtonText
        binding.buttonNegative.text = negativeButtonText

        binding.buttonPositive.setOnClickListener {
            handlePositiveButtonClick(dialogType)
            dismiss()
        }

        binding.buttonNegative.setOnClickListener {
            dismiss()
        }
    }

    private fun handlePositiveButtonClick(dialogType: DialogType?) {
        when (dialogType) {
            DialogType.REPORT -> {
                chatViewModel.reportUser(
                    currentUserId = arguments?.getString(ARG_CURRENT_USER_ID) ?: "",
                    ownerId = arguments?.getString(ARG_OWNER_ID) ?: "",
                    buyerId = arguments?.getString(ARG_BUYER_ID) ?: "",
                    carId = arguments?.getString(ARG_CAR_ID) ?: ""
                )
                if (binding.checkboxBlockChat.isChecked) {
                    chatViewModel.blockUser(
                        currentUserId = arguments?.getString(ARG_CURRENT_USER_ID) ?: "",
                        ownerId = arguments?.getString(ARG_OWNER_ID) ?: "",
                        buyerId = arguments?.getString(ARG_BUYER_ID) ?: "",
                        carId = arguments?.getString(ARG_CAR_ID) ?: ""
                    )
                    onActionCompleted?.invoke()
                }
            }
            DialogType.BLOCK -> {
                chatViewModel.blockUser(
                    currentUserId = arguments?.getString(ARG_CURRENT_USER_ID) ?: "",
                    ownerId = arguments?.getString(ARG_OWNER_ID) ?: "",
                    buyerId = arguments?.getString(ARG_BUYER_ID) ?: "",
                    carId = arguments?.getString(ARG_CAR_ID) ?: ""
                )
                onActionCompleted?.invoke()
            }
            DialogType.DELETE_CHAT -> {
                onActionCompleted?.invoke()
            }
            else -> TODO()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class DialogType { REPORT, BLOCK, DELETE_CHAT }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_SHOW_CHECKBOX = "show_checkbox"
        private const val ARG_POSITIVE_BUTTON_TEXT = "positive_button_text"
        private const val ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text"
        private const val ARG_DIALOG_TYPE = "dialog_type"
        private const val ARG_CURRENT_USER_ID = "current_user_id"
        private const val ARG_OWNER_ID = "owner_id"
        private const val ARG_CAR_ID = "car_id"
        private const val ARG_BUYER_ID = "buyer_id"

        fun newInstance(
            title: String,
            message: String,
            showCheckBox: Boolean = false,
            positiveButtonText: String = "Aceptar",
            negativeButtonText: String = "Cancelar",
            dialogType: DialogType,
            currentUserId: String,
            ownerId: String,
            carId: String,
            buyerId: String,
            onActionCompleted: (() -> Unit)? = null
        ): GlobalDialogFragment {
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putBoolean(ARG_SHOW_CHECKBOX, showCheckBox)
                putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText)
                putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                putSerializable(ARG_DIALOG_TYPE, dialogType)
                putString(ARG_CURRENT_USER_ID, currentUserId)
                putString(ARG_OWNER_ID, ownerId)
                putString(ARG_CAR_ID, carId)
                putString(ARG_BUYER_ID, buyerId)
            }
            return GlobalDialogFragment(onActionCompleted).apply { arguments = args }
        }
    }
}
