package com.example.carhive.presentation.chat.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Domain.model.Message
import com.example.carhive.R
import com.example.carhive.data.model.DownloadedFileEntity
import com.example.carhive.di.DatabaseModule
import com.example.carhive.presentation.chat.dialog.MediaViewerDialogFragment
import com.example.carhive.utils.saveFileToMediaStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChatAdapter(
    private val messages: MutableList<Message>,
    private val fragmentManager: FragmentManager,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
        private const val TAG = "ChatAdapter"
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messages[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (currentMessage.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                if (viewType == VIEW_TYPE_SENT) R.layout.item_message_sent else R.layout.item_message_received,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = if (position > 0) messages[position - 1] else null
        if (holder is MessageViewHolder) {
            holder.bind(message, previousMessage)
            // Actualiza el ícono del estado
            holder.updateStatusIcon(message.status)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        private val timeTextView: TextView = itemView.findViewById(R.id.messageTime)
        private val fileContainer: LinearLayout = itemView.findViewById(R.id.fileContainer)
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        private val fileInfoTextView: TextView = itemView.findViewById(R.id.fileInfo)
        private val retryButton: Button = itemView.findViewById(R.id.retryButton)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateHeader)
        private val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        private val fileImageView: ImageView = itemView.findViewById(R.id.fileImageView)

        fun bind(message: Message, previousMessage: Message?) {
            // Muestra la fecha si es el primer mensaje del día
            if (previousMessage == null || message.getFormattedDate() != previousMessage.getFormattedDate()) {
                dateTextView.visibility = View.VISIBLE
                dateTextView.text = message.getFormattedDate()
            } else {
                dateTextView.visibility = View.GONE
            }

            // Configuración del contenido del mensaje
            messageTextView.visibility = View.GONE
            fileContainer.visibility = View.GONE
            retryButton.visibility = View.GONE

            when {
                message.fileType?.startsWith("image/") == true -> configureImageView(message)
                message.fileType?.startsWith("video/") == true -> configureVideoView(message)
                message.fileType != null -> configureFileContainer(message) // Para archivos generales
                message.content != null -> {
                    messageTextView.visibility = View.VISIBLE
                    messageTextView.text = message.content
                }
            }

            // Muestra la hora del mensaje
            timeTextView.text = message.getFormattedTime()
        }

        private fun configureFileContainer(message: Message) {
            fileContainer.visibility = View.VISIBLE
            fileNameTextView.text = message.fileName
            fileInfoTextView.text = "${formatFileSize(message.fileSize)} • ${getFriendlyFileType(message.fileType ?: "")}"


            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                if (fileUri != null) {
                    fileContainer.setOnClickListener {
                        openFileWithApp(context, fileUri, message.fileType ?: "*/*")
                    }
                } else {
                    Toast.makeText(context, "Archivo no encontrado o descargando.", Toast.LENGTH_SHORT).show()
                }
            }

            retryButton.setOnClickListener {
                downloadAndSaveFile(context, message) { /* Acción manejada en el callback */ }
            }
        }

        private fun configureImageView(message: Message) {
            fileImageView.visibility = View.VISIBLE
            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                fileUri?.let {
                    Glide.with(context)
                        .load(it)
                        .placeholder(R.drawable.ic_img)
                        .error(R.drawable.ic_error)
                        .into(fileImageView)
                    fileImageView.setOnClickListener {
                        openMediaModal(messages.indexOf(message))
                    }
                }
            }
        }

        private fun configureVideoView(message: Message) {
            fileImageView.visibility = View.VISIBLE
            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                fileUri?.let {
                    Glide.with(context)
                        .load(it)
                        .placeholder(R.drawable.ic_video)
                        .error(R.drawable.ic_error)
                        .into(fileImageView)
                    fileImageView.setOnClickListener {
                        openMediaModal(messages.indexOf(message))
                    }
                }
            }
        }

        private fun downloadAndSaveFile(context: Context, message: Message, onFileReady: (Uri?) -> Unit) {
            val fileUrl = message.fileUrl
            val fileHash = message.hash ?: return
            val fileName = message.fileName ?: "archivo_descargado"

            val downloadedFileDao = DatabaseModule.getDatabase(context).downloadedFileDao()

            coroutineScope.launch {
                val existingFile = downloadedFileDao.getFileByHash(fileHash)

                if (existingFile != null) {
                    Log.d(TAG, "Archivo ya existe en la base de datos: ${existingFile.filePath}, omitiendo descarga.")
                    onFileReady(Uri.parse(existingFile.filePath))
                    return@launch
                }

                retryButton.visibility = View.GONE

                if (fileUrl != null) {
                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
                    storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val fileUri = message.fileType?.let {
                            saveFileToMediaStorage(context, bytes, it, fileHash, fileName)
                        }
                        if (fileUri != null) {
                            Log.d(TAG, "Archivo guardado exitosamente en $fileUri")

                            val filePath = fileUri.toString()
                            val downloadedFile = DownloadedFileEntity(fileHash, fileName, filePath, message.fileType ?: "application/octet-stream")
                            coroutineScope.launch { downloadedFileDao.insertFile(downloadedFile) }
                            onFileReady(fileUri)
                        } else {
                            showRetryButton()
                            onFileReady(null)
                        }
                    }.addOnFailureListener {
                        showRetryButton()
                        Log.e(TAG, "Error al descargar el archivo: ${it.message}", it)
                        onFileReady(null)
                    }
                } else {
                    showRetryButton()
                    onFileReady(null)
                }
            }
        }

        private fun openFileWithApp(context: Context, fileUri: Uri, mimeType: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.grantUriPermission(context.packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No hay aplicaciones para abrir este archivo.", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getFriendlyFileType(mimeType: String): String {
            return when {
                mimeType.startsWith("application/pdf") -> "pdf"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> "docx"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> "xlsx"
                mimeType.startsWith("text/plain") -> "txt"
                mimeType.startsWith("text/x-java") -> "java"
                else -> "archivo"
            }
        }

        private fun formatFileSize(fileSizeInBytes: Long): String {
            val kiloBytes = fileSizeInBytes / 1024
            val megaBytes = kiloBytes / 1024
            return when {
                megaBytes > 0 -> "$megaBytes MB"
                kiloBytes > 0 -> "$kiloBytes KB"
                else -> "$fileSizeInBytes B"
            }
        }

        private fun showRetryButton() {
            retryButton.visibility = View.VISIBLE
            Toast.makeText(itemView.context, "Error al descargar el archivo. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
        }

        fun updateStatusIcon(status: String) {
            when (status) {
                "sent" -> statusIcon.setImageResource(R.drawable.ic_sent)
                "read" -> statusIcon.setImageResource(R.drawable.ic_read)
                "failed" -> statusIcon.setImageResource(R.drawable.ic_failed)
            }
        }

        private fun openMediaModal(position: Int) {
            val mediaViewerDialog = MediaViewerDialogFragment.newInstance(messages, position)
            mediaViewerDialog.show(fragmentManager, "MediaViewerDialog")
        }
    }
}
