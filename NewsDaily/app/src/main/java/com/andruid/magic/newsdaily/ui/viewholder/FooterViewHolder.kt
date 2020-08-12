package com.andruid.magic.newsdaily.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andruid.magic.newsdaily.databinding.LayoutFooterBinding

class FooterViewHolder(binding: LayoutFooterBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): FooterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LayoutFooterBinding.inflate(inflater, parent, false)

            return FooterViewHolder(binding)
        }
    }
}