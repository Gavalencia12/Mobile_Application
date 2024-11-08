package com.example.carhive.presentation.chat.dialog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.carhive.Domain.model.Message
import com.example.carhive.R

class MediaViewerDialogFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var topBar: LinearLayout
    private lateinit var closeButton: ImageView
    private var messages: List<Message> = emptyList()
    private var initialPosition: Int = 0
    private var isTopBarVisible = false

    companion object {
        private const val TAG = "MediaViewerDialogFragment"

        fun newInstance(messages: List<Message>, position: Int): MediaViewerDialogFragment {
            return MediaViewerDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("messages", ArrayList(messages))
                    putInt("position", position)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_media_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        topBar = view.findViewById(R.id.topBar)
        closeButton = view.findViewById(R.id.closeButton)

        // Obtener la lista completa de mensajes
        val originalMessages = arguments?.getParcelableArrayList<Message>("messages") ?: emptyList()

        // Obtener la posición inicial original
        val originalPosition = arguments?.getInt("position") ?: 0
        val initialMessageId = originalMessages.getOrNull(originalPosition)?.messageId

        // Filtrar los mensajes para incluir solo imágenes o videos
        messages = originalMessages.filter { message ->
            message.fileType?.startsWith("image/") == true || message.fileType?.startsWith("video/") == true
        }

        // Calcular la nueva posición inicial en la lista filtrada
        initialPosition = messages.indexOfFirst { it.messageId == initialMessageId }
        if (initialPosition == -1) {
            initialPosition = 0 // En caso de que no se encuentre el mensaje original en la lista filtrada
        }

        mediaAdapter = MediaAdapter(messages, this)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(initialPosition, false)

        // Configurar el botón de cierre
        closeButton.setOnClickListener {
            dismiss() // Cierra el modal
        }
    }

    fun toggleTopBar() {
        isTopBarVisible = !isTopBarVisible
        topBar.visibility = if (isTopBarVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.black) // Fondo negro en toda la pantalla
    }

    inner class MediaAdapter(
        private val items: List<Message>,
        private val fragment: MediaViewerDialogFragment // Recibe el fragmento como parámetro
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_IMAGE = 0
        private val VIEW_TYPE_VIDEO = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_IMAGE) {
                ImageViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_media_image, parent, false)
                )
            } else {
                VideoViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_media_video, parent, false)
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = items[position]
            if (holder is ImageViewHolder) {
                holder.bind(message)
            } else if (holder is VideoViewHolder) {
                holder.bind(message)
            }
        }

        override fun getItemCount() = items.size

        override fun getItemViewType(position: Int): Int {
            return if (items[position].fileType?.startsWith("image/") == true) VIEW_TYPE_IMAGE else VIEW_TYPE_VIDEO
        }

        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.imageViewFull)

            fun bind(message: Message) {
                val fileUri = getLocalFileUri(itemView.context, message.fileName, message.fileType)
                if (fileUri != null) {
                    Log.d(TAG, "Cargando imagen desde almacenamiento local: $fileUri")
                    Glide.with(itemView.context)
                        .load(fileUri)
                        .placeholder(R.drawable.ic_img)
                        .error(R.drawable.ic_error)
                        .into(imageView)
                } else {
                    message.fileUrl?.let { url ->
                        Log.d(TAG, "Cargando imagen desde URL remota: $url")
                        Glide.with(itemView.context)
                            .load(url)
                            .placeholder(R.drawable.ic_img)
                            .error(R.drawable.ic_error)
                            .into(imageView)
                    } ?: Toast.makeText(itemView.context, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                }

                // Configurar el clic en la imagen para mostrar/ocultar la barra superior
                imageView.setOnClickListener {
                    fragment.toggleTopBar() // Llama a toggleTopBar() en el fragmento
                }
            }
        }

        inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val videoView: VideoView = itemView.findViewById(R.id.videoViewFull)
            private val playPauseButton: ImageView = itemView.findViewById(R.id.playPauseButton)
            private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            private val currentTime: TextView = itemView.findViewById(R.id.currentTime)
            private val totalTime: TextView = itemView.findViewById(R.id.totalTime)
            private val clickOverlay: View = itemView.findViewById(R.id.clickOverlay) // Nueva capa para clics

            private var isPlaying = false
            private val handler = android.os.Handler()

            fun bind(message: Message) {
                val fileUri = getLocalFileUri(itemView.context, message.fileName, message.fileType)
                val videoUri = fileUri ?: message.fileUrl?.let { Uri.parse(it) }

                if (videoUri != null) {
                    videoView.setVideoURI(videoUri)
                } else {
                    Toast.makeText(itemView.context, "No se pudo cargar el video", Toast.LENGTH_SHORT).show()
                }

                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    totalTime.text = formatTime(videoView.duration)
                    seekBar.max = videoView.duration

                    // Actualiza la barra de progreso cada segundo
                    handler.post(updateSeekBar)
                }

                videoView.setOnCompletionListener {
                    isPlaying = false
                    playPauseButton.setImageResource(R.drawable.ic_play)
                }

                playPauseButton.setOnClickListener {
                    if (isPlaying) {
                        videoView.pause()
                        playPauseButton.setImageResource(R.drawable.ic_play)
                    } else {
                        videoView.start()
                        playPauseButton.setImageResource(R.drawable.ic_pause)
                    }
                    isPlaying = !isPlaying
                }

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            videoView.seekTo(progress)
                            currentTime.text = formatTime(videoView.currentPosition)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                // Configurar el clic en el overlay para mostrar/ocultar la barra superior
                clickOverlay.setOnClickListener {
                    fragment.toggleTopBar() // Llama a toggleTopBar() en el fragmento
                }
            }

            // Formatear el tiempo en minutos:segundos
            private fun formatTime(milliseconds: Int): String {
                val minutes = milliseconds / 1000 / 60
                val seconds = (milliseconds / 1000) % 60
                return String.format("%02d:%02d", minutes, seconds)
            }

            // Actualiza la barra de progreso y el tiempo actual
            private val updateSeekBar = object : Runnable {
                override fun run() {
                    seekBar.progress = videoView.currentPosition
                    currentTime.text = formatTime(videoView.currentPosition)
                    handler.postDelayed(this, 1000) // Actualizar cada segundo
                }
            }
        }

        private fun getLocalFileUri(context: Context, fileName: String?, fileType: String?): Uri? {
            val mediaDir = context.getExternalFilesDir(null)?.parentFile?.resolve("media/${context.packageName}")

            // Determina la subcarpeta en base al tipo de archivo
            val subDir = when {
                fileType?.startsWith("image/") == true -> "images"
                fileType?.startsWith("video/") == true -> "videos"
                else -> "documents"
            }

            // Construye la ruta completa del archivo
            val file = mediaDir?.resolve("$subDir/${fileName ?: "unknown_file"}")
            val uri = if (file?.exists() == true) Uri.fromFile(file) else null
            Log.d(TAG, "getLocalFileUri: fileName=$fileName, uri=$uri, fileType=$fileType")
            return uri
        }

    }
}
