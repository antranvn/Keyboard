package com.securekey.sample.xml.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.securekey.sdk.credentials.createCredential
import com.securekey.sdk.credentials.getCredential
import com.securekey.sample.databinding.FragmentLoginBinding
import com.securekey.sample.ui.screens.LoginNavEvent
import com.securekey.sample.ui.screens.LoginViewModel
import com.securekey.sample.xml.otp.OtpActivity
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

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

        SecureKey.getInstance().attachTo(binding.passwordField, KeyboardMode.QWERTY_FULL)

        val activity = requireActivity()
        Log.d(TAG, "onViewCreated: activity=${activity::class.simpleName}")

        SecureKey.getInstance().setKeyboardAction(
            label = getString(com.securekey.sample.R.string.login_sign_in_with_saved)
        ) {
            Log.d(TAG, "savedPassword tapped (keyboard action)")
            viewModel.signInWithSavedPassword(
                getCredential = { req -> getCredential(activity, req) },
                onFilled = { id, pw ->
                    Log.d(TAG, "onFilled: id=$id passwordLength=${pw.length}")
                    binding.usernameField.setText(id)
                    binding.passwordField.setText(pw)
                }
            )
        }

        binding.signInButton.setOnClickListener {
            val username = binding.usernameField.text?.toString().orEmpty()
            val password = binding.passwordField.text?.toString().orEmpty()
            Log.d(TAG, "signIn tapped: username.blank=${username.isBlank()} password.blank=${password.isBlank()}")
            viewModel.saveAndProceed(
                username = username,
                password = password,
                createCredential = { req -> createCredential(activity, req) }
            )
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { loading ->
                        Log.d(TAG, "isLoading = $loading")
                        binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.signInButton.isEnabled = !loading
                    }
                }
                launch {
                    viewModel.statusMessage.collect { msg ->
                        msg?.let { Log.d(TAG, "status: $it") }
                        renderMessage(msg, isError = false)
                    }
                }
                launch {
                    viewModel.errorMessage.collect { msg ->
                        msg?.let { Log.w(TAG, "error: $it") }
                        renderMessage(msg, isError = true)
                    }
                }
                launch {
                    viewModel.navigationEvent.collect { event ->
                        Log.d(TAG, "nav event: $event")
                        when (event) {
                            is LoginNavEvent.NavigateToHome -> {
                                startActivity(Intent(requireContext(), OtpActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderMessage(msg: String?, isError: Boolean) {
        if (msg == null) {
            if (!isError) binding.statusText.visibility = View.GONE
            return
        }
        binding.statusText.text = msg
        binding.statusText.visibility = View.VISIBLE
        binding.statusText.setTextColor(
            if (isError) {
                com.google.android.material.R.attr.colorError.let {
                    val tv = android.util.TypedValue()
                    requireContext().theme.resolveAttribute(it, tv, true)
                    tv.data
                }
            } else {
                android.R.attr.textColorSecondary.let {
                    val tv = android.util.TypedValue()
                    requireContext().theme.resolveAttribute(it, tv, true)
                    tv.data
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SecureKey.getInstance().clearKeyboardAction()
        _binding = null
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}
