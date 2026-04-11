package com.securekey.sample.xml.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.securekey.sample.databinding.FragmentLoginBinding
import com.securekey.sample.xml.otp.OtpActivity
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Password field opts into SecureKey. Username field is deliberately
        // left untouched so it shows the system IME — demonstrating that the
        // SDK only intercepts the fields you explicitly attach.
        SecureKey.getInstance().attachTo(binding.passwordField, KeyboardMode.QWERTY_FULL)

        binding.signInButton.setOnClickListener {
            startActivity(Intent(requireContext(), OtpActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
