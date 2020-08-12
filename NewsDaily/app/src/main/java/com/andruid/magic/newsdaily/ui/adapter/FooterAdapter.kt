package com.andruid.magic.newsdaily.ui.adapter

import android.util.Log
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.andruid.magic.newsdaily.ui.viewholder.FooterViewHolder

class FooterAdapter : LoadStateAdapter<FooterViewHolder>() {
    override fun onBindViewHolder(holder: FooterViewHolder, loadState: LoadState) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): FooterViewHolder {
        Log.d("pageLog", "load state = $loadState")
        return FooterViewHolder.from(parent)
    }

    override fun displayLoadStateAsItem(loadState: LoadState) =
        loadState is LoadState.NotLoading && loadState.endOfPaginationReached
}