package com.securekey.sample.xml.pin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.securekey.sample.R
import com.securekey.sample.databinding.FragmentPinSetupBinding
import com.securekey.sample.xml.transfer.TransferActivity
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode
import com.securekey.sdk.ui.SecureEditText

class PinSetupFragment : Fragment() {

    private var _binding: FragmentPinSetupBinding? = null
    private val binding get() = _binding!!

    private lateinit var fields: List<SecureEditText>
    private var firstPin: String? = null
    private var ignoreTextChanges = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fields = listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4)

        fields.forEachIndexed { index, field ->
            SecureKey.getInstance().attachTo(field, KeyboardMode.NUMERIC_PIN)

            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (ignoreTextChanges) return
                    val text = s?.toString().orEmpty()
                    if (text.length == 1 && index < fields.lastIndex) {
                        fields[index + 1].requestFocus()
                    }
                    val full = fields.joinToString("") { it.text.toString() }
                    if (full.length == PIN_LENGTH) {
                        onPinEntered(full)
                    }
                }
            })
        }

        fields.first().requestFocus()
    }

    private fun onPinEntered(pin: String) {
        val first = firstPin
        if (first == null) {
            firstPin = pin
            binding.pinTitle.setText(R.string.pin_title)
            binding.pinSubtitle.setText(R.string.pin_subtitle_confirm)
            binding.pinError.visibility = View.INVISIBLE
            clearFields()
        } else if (pin == first) {
            startActivity(Intent(requireContext(), TransferActivity::class.java))
            requireActivity().finish()
        } else {
            firstPin = null
            binding.pinSubtitle.setText(R.string.pin_subtitle)
            binding.pinError.setText(R.string.pin_error_mismatch)
            binding.pinError.visibility = View.VISIBLE
            clearFields()
        }
    }

    private fun clearFields() {
        ignoreTextChanges = true
        fields.forEach { it.text?.clear() }
        ignoreTextChanges = false
        fields.first().requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PIN_LENGTH = 4
    }
}
