package com.example.myapplication.ui.screen

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.data.services.MainRemoteService
import com.example.myapplication.databinding.FragmentTestViewButtonSlideBinding
import com.example.mybase.core.platform.BaseFragment
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentCheckPermission : BaseFragment<FragmentTestViewButtonSlideBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_test_view_button_slide

    @Inject
    lateinit var mainRemoteService: MainRemoteService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PermissionX.init(this)
            .permissions(Manifest.permission.POST_NOTIFICATIONS)
            .explainReasonBeforeRequest()
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .onExplainRequestReason { scope, deniedList ->
                Log.d("TAG", "onViewCreated: NamTD8")
                scope.showRequestReasonDialog(
                    deniedList,
                    "Core fundamental are based on these permissions",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Toast.makeText(context, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        context,
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}