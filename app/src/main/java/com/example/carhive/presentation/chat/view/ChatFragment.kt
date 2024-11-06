package com.example.carhive.presentation.chat.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.databinding.FragmentChatBinding
import com.example.carhive.presentation.chat.adapter.ChatAdapter
import com.example.carhive.presentation.chat.viewModel.ChatViewModel
import com.example.carhive.Domain.usecase.chats.CleanUpDatabaseUseCase
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llama a la función de limpieza de la base de datos
        lifecycleScope.launch {
            cleanUpDatabaseUseCase(requireContext() )
        }

        setupRecyclerView()
        observeViewModel()

        chatViewModel.observeMessages(ownerId, carId, buyerId)

        chatViewModel.loadInfoHead(ownerId, carId, buyerId)

        requestStoragePermission()

        // Enviar mensaje de texto
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            chatViewModel.sendTextMessage(ownerId, carId, buyerId, message)
            binding.editTextMessage.text.clear()
        }

        // Adjuntar archivo
        binding.attachButton.setOnClickListener {
            openFileChooser()
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

                lifecycleScope.launch {
                    val fileHash = calculateFileHash(uri)
                    chatViewModel.sendFileMessage(ownerId, carId, buyerId, uri, fileType, fileName, fileHash)
                }
            }
        }
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
