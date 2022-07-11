package com.death14stroke.newsdaily.ui.custom

import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * QueryTextListener with debouncing rate of 500ms
 */
class DebouncingQueryTextListener(
    private val coroutineScope: CoroutineScope,
    private val onDebouncingQueryTextChange: (String?) -> Unit
) : MaterialSearchView.OnQueryTextListener {
    private val debouncePeriod = 500L
    private var searchJob: Job? = null

    override fun onQueryTextSubmit(query: String?) = true

    override fun onQueryTextChange(newText: String?): Boolean {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            newText?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(newText)
            }
        }

        return true
    }
}