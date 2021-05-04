package com.neverim.talkinghistory.data

import android.content.Context
import android.widget.Toast
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.neverim.talkinghistory.R

class PermissionsListener(private val context: Context) : MultiplePermissionsListener {
    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report != null) {
            if (!report.areAllPermissionsGranted()) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.ask_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken) {
        Toast.makeText(
            context,
            context.resources.getString(R.string.ask_permission),
            Toast.LENGTH_SHORT
        ).show()
        p1.continuePermissionRequest()
    }
}