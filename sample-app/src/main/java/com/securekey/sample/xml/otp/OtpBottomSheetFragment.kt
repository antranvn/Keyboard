package com.securekey.sample.xml.otp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.securekey.sample.databinding.FragmentOtpBottomSheetBinding
import com.securekey.sdk.SecureKey
import com.securekey.sdk.ui.SecureEditText

class OtpBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOtpBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var fields: List<SecureEditText>
    private var ignoreTextChanges = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fields = listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )

        fields.forEachIndexed { index, field ->
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
                    if (full.length == OTP_LENGTH) {
                        onComplete()
                    }
                }
            })
        }

        SecureKey.getInstance().attachOtpFields(*fields.toTypedArray(), otpLength = OTP_LENGTH)
        fields.first().requestFocus()
    }

    override fun onStart() {
        super.onStart()
        // The bottom sheet has its own window — FLAG_SECURE on the host Activity
        // doesn't cover it, so we set it on the dialog too.
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Expand so the keyboard (floating above) doesn't cover the digit row.
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun onComplete() {
        (activity as? OtpActivity)?.onOtpVerified()
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "OtpBottomSheetFragment"
        private const val OTP_LENGTH = 6

        fun newInstance() = OtpBottomSheetFragment()
    }
}
