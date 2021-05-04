package com.neverim.talkinghistory.data

import android.graphics.Bitmap
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class CharacterInfoTest {

    @Test
    fun `test character info creation`() {
        val bitmap = mockk<Bitmap>(relaxed = true)
        val result = CharacterInfo(bitmap, "test" , "no desc")
        assertTrue(result != null)
    }

}