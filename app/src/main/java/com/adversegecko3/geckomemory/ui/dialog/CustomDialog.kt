package com.adversegecko3.geckomemory.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.adversegecko3.geckomemory.databinding.DialogCustomBinding

class CustomDialog(
    private val title: String,
    private val content: String,
    private val positiveButton: String,
    private val negativeButton: String,
    private val onSubmitClickListener: () -> Unit
) : DialogFragment() {
    private lateinit var binding: DialogCustomBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCustomBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        setTexts()
        setButtonsClick()

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun setTexts() {
        if (title.isNotEmpty()) {
            binding.tvTitle.text = title
        }
        if (content.isNotEmpty()) {
            binding.tvContent.text = content
        }
        if (positiveButton.isNotEmpty()) {
            binding.btnPositiveButton.text = positiveButton
        }
        if (negativeButton.isNotEmpty()) {
            binding.btnNegativeButton.text = negativeButton
        }
    }

    private fun setButtonsClick() {
        binding.btnPositiveButton.setOnClickListener {
            onSubmitClickListener.invoke()
            dismiss()
        }
        binding.btnNegativeButton.setOnClickListener {
            dismiss()
        }
    }
}