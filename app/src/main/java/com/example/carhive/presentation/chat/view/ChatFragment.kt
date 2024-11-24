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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
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

    /**
     * Inflates the layout for the chat fragment using view binding.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initializes the RecyclerView, observes ViewModel data, and sets up click listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cleans up the database when the view is created
        lifecycleScope.launch {
            cleanUpDatabaseUseCase(requireContext())
        }
        val admin = ownerId == "TechnicalSupport"
        setupRecyclerView()
        observeViewModel()

        // Start observing messages and load user information
        chatViewModel.observeMessages(ownerId, carId, buyerId, admin)
        chatViewModel.loadInfoHead(ownerId, carId, buyerId)

        requestStoragePermission()

        binding.buttonSend.setOnClickListener {
            val originalMessage = binding.editTextMessage.text.toString().trimEnd()
            if (originalMessage.isBlank()) {
                Toast.makeText(context, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cleanedMessage = originalMessage.replace(Regex("\\n{2,}"), "\n")
            chatViewModel.sendTextMessageWithNotification(ownerId, carId, buyerId, cleanedMessage, admin)
            binding.editTextMessage.text.clear()
        }

        // Set up editor action listener for sending message on 'Done' action
        binding.editTextMessage.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val currentText = textView.text.toString().trimEnd()
                if (currentText.endsWith("\n")) {
                    textView.text = currentText.removeSuffix("\n")
                }
                true
            } else {
                false
            }
        }

        // Set up file attachment button to open file chooser
        binding.attachButton.setOnClickListener {
            openFileChooser()
        }

        // Navigation button
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Set menu button click listener and icon dynamically based on ownerId
        binding.menuButton.apply {
            if (ownerId == "TechnicalSupport") {
                setOnClickListener { showReportDialog() }
                setImageResource(R.drawable.ic_report_problem) // Change icon for "TechnicalSupport"
            } else {
                setOnClickListener { showPopupMenu(this) }
                setImageResource(R.drawable.ic_more_vert) // Default icon
            }
        }

        if (ownerId == "TechnicalSupport") {
            binding.btnFinished.visibility = View.VISIBLE
            binding.btnFinished.setOnClickListener {
                chatViewModel.findUserNode(buyerId) { result ->
                    when (result) {
                        "buyer" -> {
                            showConfirmDeleteAllMessagesDialog("buyer")
                        }
                        "seller" -> {
                            showConfirmDeleteAllMessagesDialog("seller")
                        }
                        else -> {
                            Toast.makeText(requireContext(), "User not found in buyer or seller nodes", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        val isSeller = currentUserId == ownerId
        val blockedUserId = if (isSeller) buyerId else ownerId
        chatViewModel.isUserBlocked(currentUserId, blockedUserId, carId) { isBlocked ->
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

    /**
     * Sets up the RecyclerView for displaying chat messages.
     */
    private fun setupRecyclerView() {
        val admin = ownerId == "TechnicalSupport"
        chatAdapter = ChatAdapter(mutableListOf(), childFragmentManager, viewLifecycleOwner.lifecycleScope, admin)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    /**
     * Observes data from the ViewModel to update the UI.
     */
    private fun observeViewModel() {

        chatViewModel.uploading.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                showLoadingDialog()
            } else {
                hideLoadingDialog()
            }
        }

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
        chatViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.firstName
                binding.tvCarModel.text = it.email
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_error)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }

        chatViewModel.buyerData.observe(viewLifecycleOwner) { buyer ->
            buyer?.let {
                binding.tvName.text = it.firstName
                binding.tvCarModel.text = it.email
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_error)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }
    }

    private var loadingDialog: AlertDialog? = null

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setCancelable(false)
            builder.setView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null))
            loadingDialog = builder.create()
        }
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }


    /**
     * Shows the options menu with actions to report, block, or delete the chat.
     */
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.chat_menu)
        val isSeller = currentUserId == ownerId
        val blockedUserId = if (isSeller) buyerId else ownerId

        chatViewModel.isUserBlocked(currentUserId, blockedUserId, carId) { isBlocked ->
            val blockItem = popupMenu.menu.findItem(R.id.option_block)
            blockItem.title = if (isBlocked) "Unblock" else "Block"

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.option_report -> {
                        showReportDialog()
                        true
                    }
                    R.id.option_block -> {
                        if (isBlocked) {
                            chatViewModel.unblockUser(currentUserId, blockedUserId)
                            Toast.makeText(requireContext(), "User unblocked", Toast.LENGTH_SHORT).show()
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
        val checkboxAdmin = ownerId != "TechnicalSupport"
        val reportDialog = GlobalDialogFragment.newInstance(
            title = "Report User",
            message = "Are you sure you want to report this user? This will send a sample of recent messages for review.",
            showCheckBox = checkboxAdmin,
            positiveButtonText = "Report",
            negativeButtonText = "Cancel",
            dialogType = GlobalDialogFragment.DialogType.REPORT,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                chatViewModel.setUserBlocked(true)
                chatViewModel.clearChatForUser(ownerId, carId, buyerId)
            }
        )
        reportDialog.show(parentFragmentManager, "ReportDialog")
    }

    private fun showBlockUserDialog() {
        val blockDialog = GlobalDialogFragment.newInstance(
            title = "Block User",
            message = "Are you sure you want to block this user? You won't receive any more messages from them.",
            positiveButtonText = "Block",
            negativeButtonText = "Cancel",
            dialogType = GlobalDialogFragment.DialogType.BLOCK,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                chatViewModel.setUserBlocked(true)
            }
        )
        blockDialog.show(parentFragmentManager, "BlockDialog")
    }

    private fun showConfirmDeleteChatDialog() {
        val deleteDialog = GlobalDialogFragment.newInstance(
            title = "Confirmation",
            message = "Are you sure you want to clear the chat? This action cannot be undone.",
            positiveButtonText = "Accept",
            negativeButtonText = "Cancel",
            dialogType = GlobalDialogFragment.DialogType.DELETE_CHAT,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
            onActionCompleted = {
                lifecycleScope.launch {
                    chatViewModel.clearChatForUser(ownerId, carId, buyerId)
                }
            }
        )
        deleteDialog.show(parentFragmentManager, "DeleteChatDialog")
    }

    private fun showConfirmDeleteAllMessagesDialog(directory: String) {
        val deleteDialog = GlobalDialogFragment.newInstance(
            title = "Problem solved",
            message = "Are you sure the problem is solved?",
            positiveButtonText = "Clear",
            negativeButtonText = "Cancel",
            dialogType = GlobalDialogFragment.DialogType.DELETE_CHAT,
            currentUserId = currentUserId,
            ownerId = "TechnicalSupport",
            carId = directory, // Use the carId determined dynamically
            buyerId = buyerId,
            onActionCompleted = {
                // Call the ViewModel function to delete all messages
                lifecycleScope.launch {
                    chatViewModel.deleteAllMessages(directory, buyerId)
                    findNavController().popBackStack()
                    Toast.makeText(requireContext(), "Chat has been successfully resolved.", Toast.LENGTH_SHORT).show()
                }
            }
        )
        deleteDialog.show(parentFragmentManager, "DeleteAllMessagesDialog")
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
                showFilePreview(uri, fileType, fileName)
            }
        }
    }

    private fun showFilePreview(fileUri: Uri, fileType: String, fileName: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("File Preview")

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

        builder.setPositiveButton("Send") { dialog, _ ->
            lifecycleScope.launch {
                showLoadingDialog() // Mostrar el diálogo de cargando
                val admin = ownerId == "TechnicalSupport"
                val fileHash = calculateFileHash(fileUri)
                try {
                    chatViewModel.sendFileMessageWithNotification(ownerId, carId, buyerId, fileUri, fileType, fileName, fileHash, admin)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error sending file: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    hideLoadingDialog() // Ocultar el diálogo de cargando
                }
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

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
                Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 100
        private const val REQUEST_CODE_STORAGE_PERMISSION = 101
    }
}
