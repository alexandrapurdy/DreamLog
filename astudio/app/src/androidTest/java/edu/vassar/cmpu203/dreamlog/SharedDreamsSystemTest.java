package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.ensureOnMenu;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.waitForView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

/**
 * System tests for the Shared Dreams flow (loading list, filtering, and returning to menu).
 */
@RunWith(AndroidJUnit4.class)
public class SharedDreamsSystemTest {

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    @Before
    public void setUp() {
        initializeAndSignOut();
        ensureOnMenu();
    }

    /** Navigating from menu to shared dreams shows the screen elements. */
    @Test
    public void sharedDreams_screenLoads() {
        goToSharedDreams();

        onView(withId(R.id.sharedDreamsTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.sharedDreamsSubtitle)).check(matches(isDisplayed()));
        onView(withId(R.id.loadSharedDreamsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.applySharedFilterButton)).check(matches(isDisplayed()));
        onView(withId(R.id.clearSharedFilterButton)).check(matches(isDisplayed()));

        assertRecyclerOrEmpty();
    }

    /** Filtering and clearing filters keeps the screen stable. */
    @Test
    public void sharedDreams_filterControls_keepScreenStable() {
        goToSharedDreams();

        onView(withId(R.id.usernameFilter))
                .perform(replaceText("friend"), closeSoftKeyboard());
        onView(withId(R.id.loadSharedDreamsButton)).perform(click());

        onView(withId(R.id.sharedDreamFilter))
                .perform(replaceText("theme"), closeSoftKeyboard());
        onView(withId(R.id.applySharedFilterButton)).perform(click());
        onView(withId(R.id.clearSharedFilterButton)).perform(click());

        onView(withId(R.id.sharedDreamsTitle)).check(matches(isDisplayed()));
        assertRecyclerOrEmpty();
    }

    /** Back button returns to the menu screen. */
    @Test
    public void sharedDreams_backToMenu() {
        goToSharedDreams();

        onView(withId(R.id.backToMenuButton)).perform(click());
        waitForView(R.id.inputDreamButton);
        onView(withId(R.id.dreamLogButton)).check(matches(isDisplayed()));
    }

    private void goToSharedDreams() {
        onView(withId(R.id.sharedDreamsButton)).perform(click());
        waitForView(R.id.sharedDreamsTitle);
    }

    private void assertRecyclerOrEmpty() {
        try {
            onView(withId(R.id.sharedDreamsRecycler)).check(matches(isDisplayed()));
        } catch (Throwable t) {
            onView(withId(R.id.sharedEmpty)).check(matches(isDisplayed()));
        }
    }
}
