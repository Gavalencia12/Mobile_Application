package com.example.carhive.presentation.chat.dialog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.carhive.Domain.model.Message
import com.example.carhive.R

/**
 * DialogFragment to display media (images or videos) from chat messages in a fullscreen view.
 * Allows navigation between media items using a ViewPager2.
 */
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

        /**
         * Creates a new instance of MediaViewerDialogFragment with a list of messages and initial position.
         *
         * @param messages List of messages containing media to display.
         * @param position Initial position to open the viewer.
         */
        fun newInstance(messages: List<Message>, position: Int): MediaViewerDialogFragment {
            return MediaViewerDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("messages", ArrayList(messages))
                    putInt("position", position)
                }
            }
        }
    }

    /**
     * Inflates the layout for the dialog.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_media_viewer, container, false)
    }

    /**
     * Sets up the ViewPager, navigation controls, and close button after view creation.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        topBar = view.findViewById(R.id.topBar)
        closeButton = view.findViewById(R.id.closeButton)

        // Retrieve messages and initial position from arguments
        val originalMessages = arguments?.getParcelableArrayList<Message>("messages") ?: emptyList()
        val originalPosition = arguments?.getInt("position") ?: 0
        val initialMessageId = originalMessages.getOrNull(originalPosition)?.messageId

        // Filter messages to only include images or videos
        messages = originalMessages.filter { message ->
            message.fileType?.startsWith("image/") == true || message.fileType?.startsWith("video/") == true
        }

        // Set the initial position based on the filtered list
        initialPosition = messages.indexOfFirst { it.messageId == initialMessageId }.takeIf { it != -1 } ?: 0

        mediaAdapter = MediaAdapter(messages, this)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(initialPosition, false)

        // Configure the close button
        closeButton.setOnClickListener {
            dismiss() // Close the modal
        }
    }

    /**
     * Toggles the visibility of the top bar (e.g., for media controls).
     */
    fun toggleTopBar() {
        isTopBarVisible = !isTopBarVisible
        topBar.visibility = if (isTopBarVisible) View.VISIBLE else View.GONE
    }

    /**
     * Ensures full screen and dark background when the dialog starts.
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.black)
    }

    /**
     * Adapter for displaying media items (images or videos) in the ViewPager.
     */
    inner class MediaAdapter(
        private val items: List<Message>,
        private val fragment: MediaViewerDialogFragment
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

        /**
         * ViewHolder for displaying images in the ViewPager.
         */
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.imageViewFull)

            /**
             * Binds an image message to the ImageView, loading it from local storage or URL.
             */
            fun bind(message: Message) {
                val fileUri = getLocalFileUri(itemView.context, message.fileName, message.fileType)
                if (fileUri != null) {
                    Glide.with(itemView.context)
                        .load(fileUri)
                        .placeholder(R.drawable.ic_img)
                        .error(R.drawable.ic_error)
                        .into(imageView)
                } else {
                    message.fileUrl?.let { url ->
                        Glide.with(itemView.context)
                            .load(url)
                            .placeholder(R.drawable.ic_img)
                            .error(R.drawable.ic_error)
                            .into(imageView)
                    } ?: Toast.makeText(itemView.context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }

                // Toggle top bar visibility on image click
                imageView.setOnClickListener {
                    fragment.toggleTopBar()
                }
            }
        }

        /**
         * ViewHolder for displaying videos in the ViewPager.
         */
        inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val videoView: VideoView = itemView.findViewById(R.id.videoViewFull)
            private val playPauseButton: ImageView = itemView.findViewById(R.id.playPauseButton)
            private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            private val currentTime: TextView = itemView.findViewById(R.id.currentTime)
            private val totalTime: TextView = itemView.findViewById(R.id.totalTime)
            private val clickOverlay: View = itemView.findViewById(R.id.clickOverlay)

            private var isPlaying = false
            private val handler = Handler()

            /**
             * Binds a video message to the VideoView and sets up playback controls.
             */
            fun bind(message: Message) {
                val fileUri = getLocalFileUri(itemView.context, message.fileName, message.fileType)
                val videoUri = fileUri ?: message.fileUrl?.let { Uri.parse(it) }

                videoUri?.let {
                    videoView.setVideoURI(it)
                } ?: Toast.makeText(itemView.context, "Failed to load video", Toast.LENGTH_SHORT).show()

                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    totalTime.text = formatTime(videoView.duration)
                    seekBar.max = videoView.duration

                    handler.post(updateSeekBar) // Update the progress bar
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

                clickOverlay.setOnClickListener {
                    fragment.toggleTopBar()
                }
            }

            private fun formatTime(milliseconds: Int): String {
                val minutes = milliseconds / 1000 / 60
                val seconds = (milliseconds / 1000) % 60
                return String.format("%02d:%02d", minutes, seconds)
            }

            private val updateSeekBar = object : Runnable {
                override fun run() {
                    seekBar.progress = videoView.currentPosition
                    currentTime.text = formatTime(videoView.currentPosition)
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }

        /**
         * Retrieves a URI for a locally saved file, if it exists.
         *
         * @param context Context of the application.
         * @param fileName Name of the file to retrieve.
         * @param fileType Type of the file to determine its directory.
         * @return URI of the file if it exists, or null.
         */
        private fun getLocalFileUri(context: Context, fileName: String?, fileType: String?): Uri? {
            val mediaDir = context.getExternalFilesDir(null)?.parentFile?.resolve("media/${context.packageName}")
            val subDir = when {
                fileType?.startsWith("image/") == true -> "images"
                fileType?.startsWith("video/") == true -> "videos"
                else -> "documents"
            }
            val file = mediaDir?.resolve("$subDir/${fileName ?: "unknown_file"}")
            return if (file?.exists() == true) Uri.fromFile(file) else null
        }
    }
}
