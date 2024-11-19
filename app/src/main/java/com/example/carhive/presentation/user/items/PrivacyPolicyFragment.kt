package com.example.carhive.Presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.MainActivity
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfilePrivacyPolicyBinding
import com.google.firebase.auth.FirebaseAuth

class PrivacyPolicyFragment : Fragment() {

    // View Binding
    private var _binding: FragmentUserProfilePrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el archivo XML usando View Binding
        _binding = FragmentUserProfilePrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener {
            val isLog = FirebaseAuth.getInstance().currentUser != null
            findNavController().popBackStack()
            if(isLog) {
                (activity as MainActivity).bottomNavigationViewUser.selectedItemId = R.id.profile
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar fugas de memoria
    }
}
