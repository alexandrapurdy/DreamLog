package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

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

    private static final long UI_TIMEOUT_MS = 10_000;

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        waitForAnyView(UI_TIMEOUT_MS, R.id.authTitle, R.id.inputDreamButton);

        signInIfNeeded();

        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
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

        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
        onView(withId(R.id.userStatus)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamLogButton)).check(matches(isDisplayed()));
        onView(withId(R.id.inputDreamButton)).check(matches(isDisplayed()));
    }

    /** Menu -> ViewLog */
    private void goToViewLog() {
        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
        onView(withId(R.id.dreamLogButton)).perform(click());

        waitForView(UI_TIMEOUT_MS, R.id.dreamsRecyclerView);
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
    }

    /** if auth is showing, sign in with test  */
    private void signInIfNeeded() {
        try {
            onView(withId(R.id.authTitle)).check(matches(isDisplayed()));

            onView(withId(R.id.emailInput)).perform(
                    click(),
                    replaceText("test@example.com"),
                    closeSoftKeyboard()
            );

            onView(withId(R.id.passwordInput)).perform(
                    click(),
                    replaceText("123456"),
                    closeSoftKeyboard()
            );

            onView(withId(R.id.signInButton)).perform(click());

        } catch (Throwable ignored) {
        }
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

    private void waitForView(long timeoutMs, int viewId) {
        onView(isRoot()).perform(waitForViewAction(timeoutMs, viewId));
    }

    private void waitForAnyView(long timeoutMs, int... ids) {
        onView(isRoot()).perform(waitForAnyViewAction(timeoutMs, ids));
    }

    private static ViewAction waitForViewAction(long timeoutMs, int viewId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for view id " + viewId;
            }

            @Override
            public void perform(UiController uiController, View rootView) {
                long end = System.currentTimeMillis() + timeoutMs;

                do {
                    for (View v : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (v.getId() == viewId) return;
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < end);

                throw new AssertionError("Timed out waiting for view id: " + viewId);
            }
        };
    }

    private static ViewAction waitForAnyViewAction(long timeoutMs, int... ids) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for any of view ids";
            }

            @Override
            public void perform(UiController uiController, View rootView) {
                long end = System.currentTimeMillis() + timeoutMs;

                do {
                    for (View v : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        int vid = v.getId();
                        for (int target : ids) {
                            if (vid == target) return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < end);

                throw new AssertionError("Timed out waiting for any of the provided view ids.");
            }
        };
    }
}
