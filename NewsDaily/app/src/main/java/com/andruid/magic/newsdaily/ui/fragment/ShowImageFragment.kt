package com.andruid.magic.newsdaily.ui.fragment

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.FragmentShowImageBinding
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class ShowImageFragment : Fragment() {
    private val safeArgs by navArgs<ShowImageFragmentArgs>()

    private lateinit var binding: FragmentShowImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.smooth_transition)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.smooth_transition)

        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowImageBinding.inflate(inflater, container, false)

        loadImage()

        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.forEach { menuItem -> menuItem.isVisible = false }
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
        Picasso.get()
            .load(Uri.parse(safeArgs.imageUrl))
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                    e?.printStackTrace()
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    binding.imageView.transitionName = "iv_${safeArgs.imageUrl}"
                    binding.imageView.setImageBitmap(bitmap)
                    startPostponedEnterTransition()

                    initPinchZooming()
                }

            })
    }
}