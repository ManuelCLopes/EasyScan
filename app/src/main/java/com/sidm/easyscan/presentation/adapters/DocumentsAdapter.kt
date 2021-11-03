package com.sidm.easyscan.presentation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sidm.easyscan.R
import com.sidm.easyscan.data.model.DocumentDTO
import com.sidm.easyscan.databinding.ItemDocumentBinding
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.sidm.easyscan.presentation.ui.DetailsActivity
import com.sidm.easyscan.presentation.ui.MainActivity


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
                bindingIncoming?.imageView?.contentDescription = documentDTO.image_url
                bindingIncoming?.tvUser?.text = documentDTO.user
                bindingIncoming?.tvTimestamp?.text = documentDTO.timestamp
                bindingIncoming?.tvProcessedText?.text = documentDTO.processed_text
                bindingIncoming?.tvId?.text = documentDTO.id

            }

            init {
                itemView.findViewById<LinearLayout>(R.id.ll_message_container) .setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "TODO: Implement details screen",
                        Toast.LENGTH_SHORT
                    ).show()
                    val b = Bundle()
                    b.putString("image_url", bindingIncoming?.imageView?.contentDescription.toString())
                    b.putString("processed_text", bindingIncoming?.tvProcessedText?.text.toString())
                    b.putString("id", bindingIncoming?.tvId?.text.toString())

                    val intent = Intent(itemView.context, DetailsActivity::class.java)
                    intent.putExtras(b)
                    startActivity(itemView.context, intent, null)
                }
            }
        }

}
