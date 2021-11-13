package com.sidm.easyscan.presentation.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.data.model.DocumentDTO
import com.sidm.easyscan.databinding.ItemDocumentBinding
import com.sidm.easyscan.presentation.ui.DetailsActivity


class DocumentsAdapter : ListAdapter<DocumentDTO, DocumentsAdapter.ItemsViewHolder>(DiffCallback()) {

    private lateinit var bindingIncoming: ItemDocumentBinding

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemsViewHolder {
        bindingIncoming = ItemDocumentBinding.inflate(LayoutInflater.from(viewGroup.context))
        return ItemsViewHolder(bindingIncoming)
    }

    override fun onBindViewHolder(viewHolder: ItemsViewHolder, position: Int) {
        val doc = getItem(position)
        viewHolder.bind(doc!!)
    }

    override fun getItem(position: Int): DocumentDTO? {
        return super.getItem(itemCount - position - 1)
    }

    private class DiffCallback : DiffUtil.ItemCallback<DocumentDTO>() {

        override fun areItemsTheSame(oldItem: DocumentDTO, newItem: DocumentDTO) =
            oldItem.image_url == newItem.image_url

        override fun areContentsTheSame(oldItem: DocumentDTO, newItem: DocumentDTO) =
            oldItem == newItem
    }

    class ItemsViewHolder
    constructor(itemBinding: ItemDocumentBinding) :

        RecyclerView.ViewHolder(itemBinding.root) {

        private var bindingIncoming: ItemDocumentBinding? = itemBinding

        private val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_dummy)
            .error(R.drawable.ic_dummy)

        fun bind(documentDTO: DocumentDTO) {
            Glide.with(itemView.context)
                .load(documentDTO.image_url)
                .apply(requestOptions)
                .into(bindingIncoming?.imageView!!)
            bindingIncoming?.tvUser?.text = documentDTO.user
            bindingIncoming?.tvTimestamp?.text = documentDTO.timestamp
            bindingIncoming?.tvProcessedText?.text =
                if(documentDTO.processed_text.length > 20)
                        (documentDTO.processed_text.subSequence(0, 20).toString() + "...")
                else
                    documentDTO.processed_text
            bindingIncoming?.tvId?.text = documentDTO.id
        }

        init {
            itemView.findViewById<CardView>(R.id.ll_message_container) .setOnClickListener {

                val intent = Intent(itemView.context, DetailsActivity::class.java)
                intent.putExtra("id", bindingIncoming?.tvId?.text)
                startActivity(itemView.context, intent, null)
            }
        }
    }
}
