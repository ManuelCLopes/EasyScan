package com.sidm.easyscan.presentation.presentation.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sidm.easyscan.databinding.ItemDocumentBinding
import com.sidm.easyscan.presentation.data.model.Document
import com.sidm.easyscan.presentation.presentation.ui.fragments.AboutFragment
import kotlin.TODO as TODO1

class DocumentsAdapter : ListAdapter<Document, DocumentsAdapter.MessagesViewHolder>(DiffCallback()) {

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

    private class DiffCallback : DiffUtil.ItemCallback<Document>() {

        override fun areItemsTheSame(oldItem: Document, newItem: Document) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Document, newItem: Document) =
            oldItem == newItem
    }

    class MessagesViewHolder(itemBinding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        private var bindingIncoming: ItemDocumentBinding? = itemBinding

        fun bind(document: Document) {

                bindingIncoming?.tvUser?.text = document.user
                bindingIncoming?.tvContent?.text = document.timestamp

        }

    }
}