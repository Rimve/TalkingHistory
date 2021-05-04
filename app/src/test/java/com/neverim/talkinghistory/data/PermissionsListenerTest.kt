package com.neverim.talkinghistory.data

import android.content.Context
import android.widget.Toast
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test


class PermissionsListenerTest {

    @RelaxedMockK
    private lateinit var context: Context
    private lateinit var permListener: PermissionsListener
    private lateinit var toast: Toast

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        permListener = spyk(PermissionsListener(context))
        toast = mockk(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

//    @Test
//    fun `check permissions`() {
//        val report: MultiplePermissionsReport = mockk(relaxed = true)
//        permListener.onPermissionsChecked(report)
//        verify { permListener.onPermissionsChecked(report) }
//    }
//
//    @Test
//    fun `try to ask for permissions`() {
//        val permList: MutableList<PermissionRequest> = mockk(relaxed = true)
//        val permToken: PermissionToken = mockk(relaxed = true)
//        permListener.onPermissionRationaleShouldBeShown(permList, permToken)
//        verify { permListener.onPermissionRationaleShouldBeShown(permList, permToken) }
//    }
}