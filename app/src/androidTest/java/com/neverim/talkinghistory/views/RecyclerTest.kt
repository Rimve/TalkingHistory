package com.neverim.talkinghistory.views


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
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
class RecyclerTest {

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

    // Try to scroll to an existent character
    @Test
    fun recyclerItemWithTextThatExists() {
        Thread.sleep(2000)
        onView(withId(R.id.rv_select_char))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Lietuvių Test"))
                )
            )
    }

    // Try to scroll to not existent character
    @Test(expected = PerformException::class)
    fun recyclerItemWithTextThatDoesNotExist() {
        Thread.sleep(2000)
        onView(withId(R.id.rv_select_char))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("not existent"))
                )
            )
    }

}
