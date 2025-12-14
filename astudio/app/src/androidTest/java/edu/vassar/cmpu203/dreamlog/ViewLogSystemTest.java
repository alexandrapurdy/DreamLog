package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.ensureOnMenu;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.waitForView;

/**
 * System tests for View DreamLog use cases:
 * - Navigate to log
 * - Filter dreams
 * - Clear filter
 * - Back to menu
 *
 */
@RunWith(AndroidJUnit4.class)
public class ViewLogSystemTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    @Before
    public void setUp() {
        initializeAndSignOut();
        ensureOnMenu();
    }

    /** view Log screen loads successfully */
    @Test
    public void viewLog_screenLoads() {
        goToViewLog();

        onView(withId(R.id.filterText)).check(matches(isDisplayed()));
        onView(withId(R.id.filterButton)).check(matches(isDisplayed()));
        onView(withId(R.id.clearFilterButton)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
    }

    /** filtering with text does not crash and keeps the screen valid */
    @Test
    public void viewLog_filter_keepsScreenStable() {
        goToViewLog();

        onView(withId(R.id.filterText))
                .perform(replaceText("zzzzzzzzzzz"), closeSoftKeyboard());
        onView(withId(R.id.filterButton)).perform(click());

        onView(withId(R.id.filterText)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));

        assertRecyclerOrEmpty();
    }

    /** clear filter keeps the screen valid. */
    @Test
    public void viewLog_clearFilter_keepsScreenStable() {
        goToViewLog();

        onView(withId(R.id.filterText))
                .perform(replaceText("something"), closeSoftKeyboard());
        onView(withId(R.id.filterButton)).perform(click());

        onView(withId(R.id.clearFilterButton)).perform(click());

        onView(withId(R.id.filterText)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        assertRecyclerOrEmpty();
    }

    /** back button returns to menu. */
    @Test
    public void viewLog_backButton_returnsToMenu() {
        goToViewLog();
        onView(withId(R.id.backButton)).perform(click());

        waitForView(R.id.dreamLogButton);
        onView(withId(R.id.userStatus)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamLogButton)).check(matches(isDisplayed()));
        onView(withId(R.id.inputDreamButton)).check(matches(isDisplayed()));
    }

    /** Menu -> ViewLog */
    private void goToViewLog() {
        waitForView(R.id.dreamLogButton);
        onView(withId(R.id.dreamLogButton)).perform(click());

        waitForView(R.id.dreamsRecyclerView);
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * if you have an empty state view allow either
     */
    private void assertRecyclerOrEmpty() {
        try {
            onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
        } catch (Throwable t) {
            onView(withId(R.id.emptyMessage)).check(matches(isDisplayed()));
        }
    }
}
