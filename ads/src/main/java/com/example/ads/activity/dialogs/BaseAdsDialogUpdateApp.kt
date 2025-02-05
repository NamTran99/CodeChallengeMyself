package com.example.ads.activity.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.ads.R
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkUtilsCore

class BaseAdsDialogUpdateApp : DialogFragment() {

    private var mOnDismiss = false

    private var onDialogDismissListener: (() -> Unit)? = null

    override fun show(manager: FragmentManager, tag: String?) {
        val t = "SDKDialogUpdateApp"
        val exitDialog = manager.findFragmentByTag(t)
        if (activity?.isDestroyed == true || activity?.isFinishing == true) {
            return
        }
        if (exitDialog?.isAdded == true) {
            return
        }
        if (exitDialog == null) {
            kotlin.runCatching {
                val ft: FragmentTransaction = manager.beginTransaction()
                ft.add(this, t)
                ft.commitNow()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun show(manager: FragmentManager?) {
        manager?.let {
            show(it, tag = null)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(
            requireContext()
        )
        val view = LayoutInflater.from(context).inflate(R.layout.update_app_dialog, null, false)
        val forceUpdate = arguments?.getBoolean(FORCE_UPDATE) ?: false
        val directLink = arguments?.getString(DIRECT_LINK) ?: IKSdkDefConst.EMPTY
        val isDirectLinkValid: Boolean =
            directLink.isNotBlank() && Patterns.WEB_URL.matcher(directLink).matches()
        dialogBuilder.setView(view)
        view.findViewById<TextView>(R.id.dialogUpdateApp_confirm)?.setOnClickListener {
            if (isDirectLinkValid)
                IKSdkUtilsCore.openBrowser(context, directLink)
            else
                IKSdkUtilsCore.openStore(context, context?.packageName)
            closeDialog()
        }
        val tvCancel = view.findViewById<TextView>(R.id.dialogUpdateApp_cancel)
        if (forceUpdate) {
            tvCancel?.visibility = View.GONE
        } else {
            tvCancel?.visibility = View.VISIBLE
            tvCancel?.setOnClickListener {
                closeDialog()
            }
        }
        return dialogBuilder.create()
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            val windowParams = window?.attributes
            window?.attributes = windowParams
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        mOnDismiss = true
        onDialogDismissListener?.invoke()
        super.onDismiss(dialog)
    }

    fun getStringRes(@StringRes res: Int): String {
        return try {
            context?.resources?.getString(res) ?: IKSdkDefConst.EMPTY
        } catch (e: Exception) {
            IKSdkDefConst.EMPTY
        }
    }

    private fun closeDialog() {
        kotlin.runCatching {
            dismiss()
        }.onFailure {
            kotlin.runCatching {
                dismissAllowingStateLoss()
            }
        }
    }

    companion object {
        private const val FORCE_UPDATE = "BaseAds_forceUpdate"
        private const val DIRECT_LINK = "BaseAds_directLink"
        fun newInstance(forceUpdate: Boolean, directLink: String?): BaseAdsDialogUpdateApp {
            val args = Bundle()
            args.putBoolean(FORCE_UPDATE, forceUpdate)
            args.putString(DIRECT_LINK, directLink ?: IKSdkDefConst.EMPTY)
            val fragment = BaseAdsDialogUpdateApp()
            fragment.arguments = args
            return fragment
        }
    }

}