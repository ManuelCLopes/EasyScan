package com.sidm.easyscan.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.data.model.DocumentDTO
import com.sidm.easyscan.databinding.ItemDocumentBinding

class DocumentsAdapter : ListAdapter<DocumentDTO, DocumentsAdapter.MessagesViewHolder>(DiffCallback()) {

    private lateinit var bindingIncoming: ItemDocumentBinding

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MessagesViewHolder {
        bindingIncoming = ItemDocumentBinding.inflate(LayoutInflater.from(viewGroup.context))
        return MessagesViewHolder(bindingIncoming)
    }

    override fun onBindViewHolder(viewHolder: MessagesViewHolder, position: Int) {
        val message = getItem(position)
        viewHolder.bind(message)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].timestamp.toLong()
    }

    private class DiffCallback : DiffUtil.ItemCallback<DocumentDTO>() {

        override fun areItemsTheSame(oldItem: DocumentDTO, newItem: DocumentDTO) =
            oldItem.image_url == newItem.image_url

        override fun areContentsTheSame(oldItem: DocumentDTO, newItem: DocumentDTO) =
            oldItem == newItem
    }

    class MessagesViewHolder
        constructor(itemBinding: ItemDocumentBinding) :

        RecyclerView.ViewHolder(itemBinding.root) {
            private var bindingIncoming: ItemDocumentBinding? = itemBinding

            private val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

            fun bind(documentDTO: DocumentDTO) {
                Glide.with(itemView.context)
                    .load(documentDTO.image_url)
                    .apply(requestOptions)
                    .into(bindingIncoming?.imageView!!)
                bindingIncoming?.tvUser?.text = documentDTO.user
                bindingIncoming?.tvContent?.text = documentDTO.timestamp

            }
        }

}
