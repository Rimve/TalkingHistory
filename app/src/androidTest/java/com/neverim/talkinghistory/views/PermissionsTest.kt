package com.neverim.talkinghistory.views


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class PermissionsTest {

    private val PERMISSIONS_DIALOG_DELAY = 2000
    private val GRANT_BUTTON_INDEX = 2

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SelectorActivity::class.java)

    @Test
    fun permissionsTest() {
        sleep(1000)
        allowPermissionsIfNeeded(READ_EXTERNAL_STORAGE)
        allowPermissionsIfNeeded(RECORD_AUDIO)

        assertTrue(hasNeededPermission(READ_EXTERNAL_STORAGE))
        assertTrue(hasNeededPermission(RECORD_AUDIO))
    }

    private fun allowPermissionsIfNeeded(permissionNeeded: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(permissionNeeded)) {
                sleep(PERMISSIONS_DIALOG_DELAY.toLong())
                val device: UiDevice = UiDevice.getInstance(getInstrumentation())
                val allowPermissions: UiObject = device.findObject(
                    UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(GRANT_BUTTON_INDEX)
                )
                if (allowPermissions.exists()) {
                    allowPermissions.click()
                }
            }
        } catch (e: UiObjectNotFoundException) {
            println("Permissions dialogue is missing")
        }
    }

    private fun hasNeededPermission(permissionNeeded: String): Boolean {
        val context: Context = getInstrumentation().targetContext
        val permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            throw RuntimeException("Cannot execute Thread.sleep()")
        }
    }
}
