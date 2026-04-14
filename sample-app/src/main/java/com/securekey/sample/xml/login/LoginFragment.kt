package com.securekey.sample.xml.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.fragment.app.Fragment
import com.securekey.sample.databinding.FragmentLoginBinding
import com.securekey.sample.xml.otp.OtpActivity
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.ui.SecureEditText

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

        // Enable autofill so password managers can fill this field
        (binding.passwordField as? SecureEditText)?.enableAutofill(View.AUTOFILL_HINT_PASSWORD)

        binding.signInButton.setOnClickListener {
            // Tell the autofill framework that the user is submitting credentials.
            // This triggers the password manager's "Save password?" prompt so the
            // credentials are available for autofill on future visits.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().getSystemService(AutofillManager::class.java)?.commit()
            }
            startActivity(Intent(requireContext(), OtpActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
