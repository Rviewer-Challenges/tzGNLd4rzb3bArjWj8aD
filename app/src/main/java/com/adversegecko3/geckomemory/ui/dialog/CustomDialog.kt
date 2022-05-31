package com.adversegecko3.geckomemory.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.adversegecko3.geckomemory.databinding.DialogCustomBinding

class CustomDialog(
    private val title: String,
    private val content: String,
    private val positiveButton: String,
    private val onSubmitClickListener: () -> Unit,
    private val negativeButton: String = "",
    private val onDismissClickListener: () -> Unit = {},
    private val isEndDialog: Boolean = false,
) : DialogFragment() {
    private lateinit var binding: DialogCustomBinding
    private var isNeutral = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCustomBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        // If the dialog is called when the game has ended, disable closing on outside click
        if (isEndDialog) {
            isCancelable = false
        }

        checkButtons()
        setTexts()
        setButtonsClick()

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun checkButtons() {
        // First check if sent positive or negative texts (but not both) are empty
        // If so, set isNeutral to true, show NeutralButton button and hide Both Buttons LinearLayout
        if ((positiveButton.isEmpty() || negativeButton.isEmpty()) &&
            !(positiveButton.isEmpty() && negativeButton.isEmpty())
        ) {
            isNeutral = true
            binding.llBothButtons.visibility = View.GONE
            binding.btnNeutralButton.visibility = View.VISIBLE
        }
    }

    private fun setTexts() {
        // Set texts to each corresponding TextView, first checking if they are not empty
        if (title.isNotEmpty()) {
            binding.tvTitle.text = title
        }
        if (content.isNotEmpty()) {
            binding.tvContent.text = content
        }

        // If checkButtons() has set isNeutral to true, set text to NeutralButton
        // If isNeutral is still false, set texts to Positive and Negative Buttons
        if (isNeutral) {
            if (positiveButton.isNotEmpty()) {
                binding.btnNeutralButton.text = positiveButton
            }
        } else {
            if (positiveButton.isNotEmpty()) {
                binding.btnPositiveButton.text = positiveButton
            }
            if (negativeButton.isNotEmpty()) {
                binding.btnNegativeButton.text = negativeButton
            }
        }
    }

    private fun setButtonsClick() {
        // If checkButtons() has set isNeutral to true, set ClickListener to NeutralButton
        // If isNeutral is still false, set ClickListeners to Positive and Negative Buttons
        if (isNeutral) {
            binding.btnNeutralButton.setOnClickListener {
                onSubmitClickListener.invoke()
                dismiss()
            }
        } else {
            binding.btnPositiveButton.setOnClickListener {
                onSubmitClickListener.invoke()
                dismiss()
            }
            binding.btnNegativeButton.setOnClickListener {
                onDismissClickListener.invoke()
                dismiss()
            }
        }
    }
}