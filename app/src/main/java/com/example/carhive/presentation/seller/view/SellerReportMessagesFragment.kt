package com.example.carhive.presentation.seller.view

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
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carhive.Domain.usecase.chats.CleanUpDatabaseUseCase
import com.example.carhive.R
import com.example.carhive.databinding.DialogFilePreviewBinding
import com.example.carhive.databinding.FragmentChatBinding
import com.example.carhive.presentation.chat.adapter.ChatAdapter
import com.example.carhive.presentation.chat.dialog.GlobalDialogFragment
import com.example.carhive.presentation.chat.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class SellerReportMessagesFragment : Fragment() {

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        setupRecyclerView()
        observeViewModel()

        setupInitialOptions()

        // Start observing messages and load user information
        chatViewModel.observeMessages(ownerId, carId, buyerId, admin = false)
        chatViewModel.loadInfoHead(ownerId, carId, buyerId)

        requestStoragePermission()

        binding.buttonSend.setOnClickListener {
            val originalMessage = binding.editTextMessage.text.toString().trimEnd()
            if (originalMessage.isBlank()) {
                Toast.makeText(context, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cleanedMessage = originalMessage.replace(Regex("\\n{2,}"), "\n")
            chatViewModel.sendTextMessage(ownerId, carId, buyerId, cleanedMessage, admin = false)
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

        // Menu button for additional chat options
        binding.menuButton.setOnClickListener {
            binding.menuButton.setImageResource(R.drawable.ic_report_problem)
            showReportDialog()
        }

    }

    private fun setupInitialOptions() {
        val optionsContainer = binding.root.findViewById<LinearLayout>(R.id.initialOptionsContainer)

        if (chatAdapter.itemCount > 0) {
            optionsContainer.visibility = View.GONE
            return
        }

        optionsContainer.visibility = View.VISIBLE

        val button1 = binding.root.findViewById<Button>(R.id.option1)
        val button2 = binding.root.findViewById<Button>(R.id.option2)
        val button3 = binding.root.findViewById<Button>(R.id.option3)
        val button4 = binding.root.findViewById<Button>(R.id.option4)
        val button5 = binding.root.findViewById<Button>(R.id.option5)

        val optionClickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.option1 -> sendOptionMessage("What information can I save about my car on the platform?")
                R.id.option2 -> sendOptionMessage("How do I register a new vehicle?")
                R.id.option3 -> sendOptionMessage("What services does carhive offer?")
                R.id.option4 -> sendOptionMessage("Is carhive available in my region?")
                R.id.option5 -> sendOptionMessage("How do I reset my password?")
            }

            // Ocultar todas las opciones después de seleccionar una
            optionsContainer.removeAllViews()
            optionsContainer.visibility = View.GONE
        }

        button1.setOnClickListener(optionClickListener)
        button2.setOnClickListener(optionClickListener)
        button3.setOnClickListener(optionClickListener)
        button4.setOnClickListener(optionClickListener)
        button5.setOnClickListener(optionClickListener)
    }

    private fun sendOptionMessage(content: String) {
        chatViewModel.sendTextMessage(ownerId, carId, buyerId, content, admin = false)
    }

    /**
     * Sets up the RecyclerView for displaying chat messages.
     */
    private fun setupRecyclerView() {
        chatAdapter =
            ChatAdapter(mutableListOf(), childFragmentManager, viewLifecycleOwner.lifecycleScope, admin = false)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    /**
     * Observes data from the ViewModel to update the UI.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            chatViewModel.messages.collect { messages ->
                chatAdapter.updateMessages(messages)

                // Ocultar las opciones iniciales si hay mensajes
                val optionsContainer =
                    binding.root.findViewById<LinearLayout>(R.id.initialOptionsContainer)
                if (messages.isNotEmpty()) {
                    optionsContainer.visibility = View.GONE
                }

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

        binding.tvName.text = "Technical Support"
        binding.tvCarModel.text = "Send a message"
        Glide.with(requireContext())
            .load(R.drawable.car_hive_logo)
            .placeholder(R.drawable.ic_img)
            .error(R.drawable.ic_error)
            .into(binding.profileImage)

    }

    /**
     * Shows the options menu with actions to report, block, or delete the chat.
     */


    private fun showReportDialog() {
        val ownerId = "Admin"
        val reportDialog = GlobalDialogFragment.newInstance(
            title = "Report User",
            message = "Are you sure you want to report this user? This will send a sample of recent messages for review.",
            positiveButtonText = "Report",
            negativeButtonText = "Cancel",
            dialogType = GlobalDialogFragment.DialogType.REPORT,
            currentUserId = currentUserId,
            ownerId = ownerId,
            carId = carId,
            buyerId = buyerId,
        )
        reportDialog.show(parentFragmentManager, "ReportDialog")
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
                val fileType =
                    requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
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
                val fileHash = calculateFileHash(fileUri)
                chatViewModel.sendFileMessage(
                    ownerId,
                    carId,
                    buyerId,
                    fileUri,
                    fileType,
                    fileName,
                    fileHash,
                    admin = false
                )
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
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(permission),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 100
        private const val REQUEST_CODE_STORAGE_PERMISSION = 101
    }
}
