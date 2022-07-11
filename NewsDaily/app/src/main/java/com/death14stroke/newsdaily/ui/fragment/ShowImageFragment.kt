package com.death14stroke.newsdaily.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import coil.load
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.databinding.FragmentShowImageBinding
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import timber.log.Timber

class ShowImageFragment : Fragment(R.layout.fragment_show_image) {
    private val binding by viewBinding(FragmentShowImageBinding::bind)
    private val safeArgs by navArgs<ShowImageFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.smooth_transition)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.smooth_transition)

        postponeEnterTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }

    private fun initPinchZooming() {
        createLoupe(binding.imageView, binding.container) {
            useFlingToDismissGesture = false
            setOnViewTranslateListener(
                onDismiss = { findNavController().navigateUp() }
            )
        }
    }

    private fun loadImage() {
        binding.imageView.load(safeArgs.imageUrl) {
            error(R.drawable.default_news)
            listener(
                onSuccess = { _, _ -> beginTransition() },
                onError = { _, e ->
                    Timber.e("Could not load image = ${safeArgs.imageUrl}", e.throwable)
                    beginTransition()
                }
            )
        }
    }

    /**
     * Setup shared element transition effect with pinch zooming
     */
    private fun beginTransition() {
        binding.imageView.transitionName = "iv_${safeArgs.imageUrl}"
        startPostponedEnterTransition()
        initPinchZooming()
    }
}