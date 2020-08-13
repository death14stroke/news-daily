package com.andruid.magic.newsdaily.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.EXTRA_BACKGROUND_COLOR
import com.andruid.magic.newsdaily.data.EXTRA_DESC_RES
import com.andruid.magic.newsdaily.data.EXTRA_LOTTIE_RES
import com.andruid.magic.newsdaily.data.EXTRA_TITLE_RES
import com.andruid.magic.newsdaily.databinding.FragmentIntroBinding
import com.andruid.magic.newsdaily.util.color
import com.github.appintro.SlideBackgroundColorHolder

class IntroFragment : Fragment() {
    companion object {
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
                    EXTRA_BACKGROUND_COLOR to backgroundColor,
                    EXTRA_LOTTIE_RES to lottieRes
                )
            }
    }

    private lateinit var binding: FragmentIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroBinding.inflate(inflater, container, false)

        arguments?.let { args ->
            binding.titleTV.setText(args.getInt(EXTRA_TITLE_RES))
            binding.messageTV.setText(args.getInt(EXTRA_DESC_RES))

            binding.container.setBackgroundResource(args.getInt(EXTRA_BACKGROUND_COLOR))

            val lottieRes = args.getInt(EXTRA_LOTTIE_RES)
            binding.animationView.setAnimation(lottieRes)
            //binding.animationView.playAnimation()
        }

        return binding.root
    }
}