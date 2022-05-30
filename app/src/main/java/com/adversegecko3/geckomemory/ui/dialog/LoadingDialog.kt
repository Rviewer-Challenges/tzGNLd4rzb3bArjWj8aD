package com.adversegecko3.geckomemory.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.adversegecko3.geckomemory.R

@SuppressLint("InflateParams")
class LoadingDialog(mActivity: Activity) {
    private var dialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(mActivity)
        val inflater = mActivity.layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}