package com.example.carhive.presentation.chat.dialog

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
    private var messages: List<Message> = emptyList()
    private var initialPosition: Int = 0

    companion object {
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
        messages = arguments?.getParcelableArrayList("messages") ?: emptyList()
        initialPosition = arguments?.getInt("position") ?: 0

        mediaAdapter = MediaAdapter(messages)
        viewPager.adapter = mediaAdapter
        viewPager.setCurrentItem(initialPosition, false)
    }

    inner class MediaAdapter(private val items: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_IMAGE = 0
        private val VIEW_TYPE_VIDEO = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_IMAGE) {
                ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_image, parent, false))
            } else {
                VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_media_video, parent, false))
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
                Glide.with(itemView.context)
                    .load(message.fileUrl)
                    .into(imageView)
            }
        }

        inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val videoView: VideoView = itemView.findViewById(R.id.videoViewFull)

            fun bind(message: Message) {
                videoView.setVideoURI(Uri.parse(message.fileUrl))
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                }
                videoView.start()
            }
        }
    }
}
