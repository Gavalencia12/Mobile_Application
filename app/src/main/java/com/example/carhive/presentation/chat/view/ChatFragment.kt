package com.example.carhive.presentation.chat.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carhive.databinding.FragmentChatBinding
import com.example.carhive.databinding.DialogFilePreviewBinding
import com.example.carhive.presentation.chat.adapter.ChatAdapter
import com.example.carhive.presentation.chat.viewModel.ChatViewModel
import com.example.carhive.Domain.usecase.chats.CleanUpDatabaseUseCase
import com.example.carhive.R
import com.example.carhive.presentation.chat.dialog.GlobalDialogFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    @Inject
    lateinit var cleanUpDatabaseUseCase: CleanUpDatabaseUseCase

    private val ownerId by lazy { arguments?.getString("ownerId") ?: "" }
    private val carId by lazy { arguments?.getString("carId") ?: "" }
    private val buyerId by lazy { arguments?.getString("buyerId") ?: "" }
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llama a la función de limpieza de la base de datos
        lifecycleScope.launch {
            cleanUpDatabaseUseCase(requireContext())
        }

        setupRecyclerView()
        observeViewModel()

        chatViewModel.observeMessages(ownerId, carId, buyerId)

        chatViewModel.loadInfoHead(ownerId, carId, buyerId)

        requestStoragePermission()

        // Enviar mensaje de texto
        binding.buttonSend.setOnClickListener {
            val originalMessage = binding.editTextMessage.text.toString().trimEnd()

            // Evita el envío de mensajes vacíos
            if (originalMessage.isBlank()) {
                Toast.makeText(context, "No se puede enviar un mensaje vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Procesa el mensaje eliminando saltos de línea innecesarios
            val cleanedMessage = originalMessage.replace(Regex("\\n{2,}"), "\n")

            chatViewModel.sendTextMessage(ownerId, carId, buyerId, cleanedMessage)
            binding.editTextMessage.text.clear()
        }

        // Configuración para el campo de entrada de texto
        binding.editTextMessage.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val currentText = textView.text.toString().trimEnd()

                // Si el contenido después del salto de línea está vacío, elimina el salto de línea
                if (currentText.endsWith("\n")) {
                    textView.text = currentText.removeSuffix("\n")
                }

                true
            } else {
                false
            }
        }

        // Adjuntar archivo
        binding.attachButton.setOnClickListener {
            openFileChooser()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        chatViewModel.isUserBlocked(currentUserId, buyerId) { isBlocked ->
            if (isBlocked) {
                binding.blockedMessageTextView.visibility = View.VISIBLE
                binding.messageInputLayout.visibility = View.GONE
                binding.buttonSend.visibility = View.GONE
            } else {
                binding.blockedMessageTextView.visibility = View.GONE
                binding.messageInputLayout.visibility = View.VISIBLE
                binding.buttonSend.visibility = View.VISIBLE
            }
        }

    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(mutableListOf(), childFragmentManager, viewLifecycleOwner.lifecycleScope)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {

        chatViewModel.isUserBlocked.observe(viewLifecycleOwner) { isBlocked ->
            if (isBlocked) {
                binding.blockedMessageTextView.visibility = View.VISIBLE
                binding.messageInputLayout.visibility = View.GONE
                binding.buttonSend.visibility = View.GONE
            } else {
                binding.blockedMessageTextView.visibility = View.GONE
                binding.messageInputLayout.visibility = View.VISIBLE
                binding.buttonSend.visibility = View.VISIBLE
            }
        }
        lifecycleScope.launch {
            chatViewModel.messages.collect { messages ->
                chatAdapter.updateMessages(messages)
                binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
            }
        }

        lifecycleScope.launch {
            chatViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observa los datos del usuario con el que estás hablando
        chatViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Actualiza la interfaz de usuario con los datos del usuario con el que estás hablando
                binding.tvName.text = user.firstName
                binding.tvCarModel.text = user.email
                Glide.with(requireContext())
                    .load(user.imageUrl)
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_error)
                    .into(binding.profileImage)
            }
        }

        // Observa los datos del comprador, en caso de que seas el vendedor
        chatViewModel.buyerData.observe(viewLifecycleOwner) { buyer ->
            if (buyer != null) {
                binding.tvName.text = buyer.firstName
                binding.tvCarModel.text = buyer.email
                Glide.with(requireContext())
                    .load(buyer.imageUrl)
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_error)
                    .into(binding.profileImage)
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.chat_menu)

        // Configura el menú según el estado de bloqueo
        chatViewModel.isUserBlocked(currentUserId, buyerId) { isBlocked ->
            val blockItem = popupMenu.menu.findItem(R.id.option_block)
            blockItem.title = if (isBlocked) "Desbloquear" else "Bloquear"

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.option_report -> {
                        showReportDialog()
                        true
                    }
                    R.id.option_block -> {
                        if (isBlocked) {
                            chatViewModel.unblockUser(currentUserId, buyerId)
                            Toast.makeText(requireContext(), "Usuario desbloqueado", Toast.LENGTH_SHORT).show()
                        } else {
                            showBlockUserDialog()
                        }
                        true
                    }
                    R.id.option_exit -> {
                        showConfirmDeleteChatDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun showReportDialog() {
        val reportDialog = GlobalDialogFragment.newInstance(
            title = "Reportar usuario",
            message = "¿Estás seguro de que deseas reportar a este usuario? Esto enviará una muestra de los mensajes recientes para revisión.",
            showCheckBox = true,
            positiveButtonText = "Reportar",
            negativeButtonText = "Cancelar",
            dialogType = GlobalDialogFragment.DialogType.REPORT,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                chatViewModel.setUserBlocked(true)
            }
        )
        reportDialog.show(parentFragmentManager, "ReportDialog")
    }

    private fun showBlockUserDialog() {
        val blockDialog = GlobalDialogFragment.newInstance(
            title = "Bloquear usuario",
            message = "¿Estás seguro de que deseas bloquear a este usuario? No podrás recibir más mensajes de ellos.",
            positiveButtonText = "Bloquear",
            negativeButtonText = "Cancelar",
            dialogType = GlobalDialogFragment.DialogType.BLOCK,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                chatViewModel.setUserBlocked(true)
                chatViewModel.clearChatForUser(ownerId, carId, buyerId)
            }
        )
        blockDialog.show(parentFragmentManager, "BlockDialog")
    }

    private fun showConfirmDeleteChatDialog() {
        val deleteDialog = GlobalDialogFragment.newInstance(
            title = "Confirmación",
            message = "¿Estás seguro de que deseas vaciar el chat? Esta acción no se puede deshacer.",
            positiveButtonText = "Aceptar",
            negativeButtonText = "Cancelar",
            dialogType = GlobalDialogFragment.DialogType.DELETE_CHAT,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                chatViewModel.clearChatForUser(ownerId, carId, buyerId)
            }
        )
        deleteDialog.show(parentFragmentManager, "DeleteChatDialog")
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
                val fileName = getFileNameFromUri(uri) ?: "file"

                // Mostrar vista previa y confirmación antes de enviar
                showFilePreview(uri, fileType, fileName)
            }
        }
    }

    private fun showFilePreview(fileUri: Uri, fileType: String, fileName: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Vista previa del archivo")

        val previewBinding = DialogFilePreviewBinding.inflate(LayoutInflater.from(requireContext()))
        builder.setView(previewBinding.root)

        when {
            fileType.startsWith("image/") -> {
                previewBinding.previewImageView.visibility = View.VISIBLE
                previewBinding.previewImageView.setImageURI(fileUri)
            }
            fileType.startsWith("video/") -> {
                previewBinding.previewVideoView.visibility = View.VISIBLE
                previewBinding.previewVideoView.setVideoURI(fileUri)
                previewBinding.previewVideoView.start()
            }
            else -> {
                previewBinding.previewFileName.visibility = View.VISIBLE
                previewBinding.previewFileName.text = fileName
            }
        }

        builder.setPositiveButton("Enviar") { dialog, _ ->
            lifecycleScope.launch {
                val fileHash = calculateFileHash(fileUri)
                chatViewModel.sendFileMessage(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash)
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun calculateFileHash(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        return inputStream?.use {
            val bytes = it.readBytes()
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { byte ->
                "%02x".format(byte)
            }
        } ?: ""
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), REQUEST_CODE_STORAGE_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 100
        private const val REQUEST_CODE_STORAGE_PERMISSION = 101
    }
}
