package com.securekey.sample.xml.transfer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.securekey.sample.R
import com.securekey.sample.databinding.FragmentTransferBinding
import com.securekey.sdk.SecureKey
import com.securekey.sdk.core.KeyboardMode

class TransferFragment : Fragment() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SecureKey.getInstance().attachTo(binding.recipientField, KeyboardMode.QWERTY_FULL)
        SecureKey.getInstance().attachTo(binding.amountField, KeyboardMode.AMOUNT_PAD)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.transferButton.isEnabled =
                    !binding.recipientField.text.isNullOrBlank() &&
                    !binding.amountField.text.isNullOrBlank()
            }
        }
        binding.recipientField.addTextChangedListener(watcher)
        binding.amountField.addTextChangedListener(watcher)

        listOf("100", "500", "1,000", "5,000").forEach { value ->
            val chip = Chip(requireContext()).apply {
                text = value
                isCheckable = false
                setOnClickListener {
                    binding.amountField.setText(value.replace(",", ""))
                    binding.amountField.setSelection(binding.amountField.text?.length ?: 0)
                }
            }
            binding.quickAmountsGroup.addView(chip)
        }

        binding.transferButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.transfer_success, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
