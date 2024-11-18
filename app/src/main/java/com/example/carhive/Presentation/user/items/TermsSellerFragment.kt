package com.example.carhive.Presentation.user.items

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.MainActivity
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerProfileTermsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsSellerFragment : Fragment() {
    private var _binding: FragmentSellerProfileTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfileTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val termsContent: TextView = binding.termsContent34

        val termsText = getString(R.string.link_privacy_policy)

        val startIndex = termsText.indexOf("Privacy Policy")
        val endIndex = startIndex + "Privacy Policy".length

        val spannableString = SpannableString(termsText).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    findNavController().navigate(R.id.action_termsSellerFragment_to_sellerPrivacyPolicyFragment)
                }
            }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        termsContent.text = spannableString
        termsContent.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        binding.close.setOnClickListener {
            findNavController().navigate(R.id.action_termsSellerFragment_to_sellerProfileFragment)
            (activity as MainActivity).bottomNavigationViewSeller.selectedItemId = R.id.profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
