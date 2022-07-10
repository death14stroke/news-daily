package com.death14stroke.newsdaily.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.databinding.FragmentIntroBinding
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding

class IntroFragment : Fragment(R.layout.fragment_intro) {
    companion object {
        private const val EXTRA_TITLE_RES = "title_res"
        private const val EXTRA_DESC_RES = "desc_res"
        private const val EXTRA_LOTTIE_RES = "lottie_res"
        private const val EXTRA_BACKGROUND_RES = "background_res"

        fun newInstance(
            @StringRes title: Int,
            @StringRes desc: Int,
            @ColorInt backgroundColor: Int,
            @RawRes lottieRes: Int
        ) =
            IntroFragment().apply {
                arguments = bundleOf(
                    EXTRA_TITLE_RES to title,
                    EXTRA_DESC_RES to desc,
                    EXTRA_BACKGROUND_RES to backgroundColor,
                    EXTRA_LOTTIE_RES to lottieRes
                )
            }
    }

    private val binding by viewBinding(FragmentIntroBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->
            binding.apply {
                titleTV.setText(args.getInt(EXTRA_TITLE_RES))
                messageTV.setText(args.getInt(EXTRA_DESC_RES))
                container.setBackgroundResource(args.getInt(EXTRA_BACKGROUND_RES))
                animationView.setAnimation(args.getInt(EXTRA_LOTTIE_RES))
            }
        }
    }
}