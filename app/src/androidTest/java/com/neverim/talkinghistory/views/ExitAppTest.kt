package com.neverim.talkinghistory.views


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.neverim.talkinghistory.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ExitAppTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SelectorActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )

    @Test(expected = NoActivityResumedException::class)
    fun exitAppTest() {

        Thread.sleep(2000)

        onView(withId(R.id.rv_select_char)).perform(pressBack())

        Thread.sleep(500)

        onView(withId(R.id.rv_select_char)).perform(pressBack())
    }
}
