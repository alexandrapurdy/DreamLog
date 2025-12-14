package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;

/**
 * System tests for Dream Detail use cases.
 */
@RunWith(AndroidJUnit4.class)
public class DreamDetailSystemTest {

    private static final long UI_TIMEOUT_MS = 10_000;

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    @Before
    public void setUp() {
        initializeAndSignOut();

        // Wait until either auth or menu is visible
        waitForAnyView(UI_TIMEOUT_MS, R.id.authTitle, R.id.inputDreamButton);

        signInIfNeeded();

        // Now we should be on menu
        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);

        ensureAtLeastOneDream();
    }

    /** Dream detail screen loads successfully. */
    @Test
    public void dreamDetail_screenLoads() {
        goToFirstDreamDetailFromLog();

        onView(withId(R.id.dreamTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamSummary)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamTheme)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamCharacters)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.analysisText)).check(matches(isDisplayed()));

        onView(withId(R.id.editButton)).check(matches(isDisplayed()));
        onView(withId(R.id.deleteButton)).check(matches(isDisplayed()));
        onView(withId(R.id.shareButton)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
    }

    /** Back button returns to ViewLog (assert on unique ViewLog views, not backButton). */
    @Test
    public void dreamDetail_backButton_returnsToLog() {
        goToFirstDreamDetailFromLog();

        onView(withId(R.id.backButton)).perform(scrollTo(), click());

        waitForView(UI_TIMEOUT_MS, R.id.dreamsRecyclerView);
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.filterText)).check(matches(isDisplayed()));
    }

    /** Edit button navigates to EditDream screen. */
    @Test
    public void dreamDetail_editButton_navigatesToEdit() {
        goToFirstDreamDetailFromLog();

        onView(withId(R.id.editButton)).perform(scrollTo(), click());

        // Edit screen has these buttons for sure (from your XML)
        waitForView(UI_TIMEOUT_MS, R.id.saveButton);
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
        onView(withId(R.id.cancelButton)).check(matches(isDisplayed()));

        // And the header text is "Edit Dream:"
        onView(withText(R.string.edit_dream)).check(matches(isDisplayed()));
    }

    /** Delete confirms, deletes, returns to ViewLog. */
    @Test
    public void dreamDetail_delete_confirm_deletesAndReturnsToLog() {
        goToFirstDreamDetailFromLog();

        onView(withId(R.id.deleteButton)).perform(scrollTo(), click());

        onView(withText(R.string.delete_dream_title)).check(matches(isDisplayed()));
        onView(withText(R.string.delete_dream_message)).check(matches(isDisplayed()));

        onView(withText(R.string.delete)).perform(click());

        waitForView(UI_TIMEOUT_MS, R.id.dreamsRecyclerView);
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));

        // snackbar
        onView(withText(R.string.dream_deleted_message))
                .perform(waitForDisplayed(UI_TIMEOUT_MS))
                .check(matches(isDisplayed()));
    }

    /** Share shows some snackbar outcome (success or failure). */
    @Test
    public void dreamDetail_share_showsOutcomeSnackbar() {
        goToFirstDreamDetailFromLog();

        onView(withId(R.id.shareButton)).perform(scrollTo(), click());

        Matcher<View> outcome =
                Matchers.anyOf(
                        withText(R.string.share_success),
                        withText(Matchers.containsString("Unable to share dream")),
                        withText(R.string.sign_in_required) // if auth state changes unexpectedly
                );

        onView(isRoot()).perform(waitForAnyMatch(outcome, UI_TIMEOUT_MS));
        onView(outcome).check(matches(isDisplayed()));
    }

    // ---------------- navigation helpers ----------------

    private void goToViewLog() {
        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
        onView(withId(R.id.dreamLogButton)).perform(click());

        waitForView(UI_TIMEOUT_MS, R.id.dreamsRecyclerView);
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
    }

    private void goToFirstDreamDetailFromLog() {
        goToViewLog();

        // If empty, create one (belt + suspenders)
        if (isEmptyStateShowing()) {
            onView(withId(R.id.backButton)).perform(click());
            waitForView(UI_TIMEOUT_MS, R.id.inputDreamButton);
            addDreamViaInputScreen("detail test", "summary", "theme", "char", "loc");
            // controller shows detail immediately; return to menu then to log
            onView(withId(R.id.backButton)).perform(scrollTo(), click()); // detail->log
            waitForView(UI_TIMEOUT_MS, R.id.backButton);
            onView(withId(R.id.backButton)).perform(click()); // log->menu
            waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
            goToViewLog();
        }

        onView(withId(R.id.dreamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        waitForView(UI_TIMEOUT_MS, R.id.dreamTitle);
        onView(withId(R.id.dreamTitle)).check(matches(isDisplayed()));
    }

    private boolean isEmptyStateShowing() {
        try {
            onView(withId(R.id.emptyMessage)).check(matches(isDisplayed()));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ---------------- data setup helpers ----------------

    private void ensureAtLeastOneDream() {
        goToViewLog();

        boolean hasItem = false;
        try {
            final boolean[] result = {false};
            scenarioRule.getScenario().onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.dreamsRecyclerView);
                if (rv != null && rv.getAdapter() != null) {
                    result[0] = rv.getAdapter().getItemCount() > 0;
                }
            });
            hasItem = result[0];
        } catch (Throwable ignored) { }

        // Back to menu
        onView(withId(R.id.backButton)).perform(click());
        waitForView(UI_TIMEOUT_MS, R.id.inputDreamButton);

        if (hasItem) return;

        addDreamViaInputScreen(
                "system test dream",
                "a summary for tests",
                "water, anxiety",
                "me, friend",
                "boston, ocean"
        );

        // Controller shows detail after add. Go back to menu for stable starting state.
        waitForView(UI_TIMEOUT_MS, R.id.backButton); // detail back
        onView(withId(R.id.backButton)).perform(scrollTo(), click()); // detail -> log
        waitForView(UI_TIMEOUT_MS, R.id.backButton); // log back
        onView(withId(R.id.backButton)).perform(click()); // log -> menu
        waitForView(UI_TIMEOUT_MS, R.id.dreamLogButton);
    }

    private void addDreamViaInputScreen(String title, String summary, String themes, String chars, String loc) {
        onView(withId(R.id.inputDreamButton)).perform(click());

        waitForView(UI_TIMEOUT_MS, R.id.dreamTitleText);

        onView(withId(R.id.dreamTitleText)).perform(scrollTo(), replaceText(title), closeSoftKeyboard());
        onView(withId(R.id.summaryText)).perform(scrollTo(), replaceText(summary), closeSoftKeyboard());
        onView(withId(R.id.themeText)).perform(scrollTo(), replaceText(themes), closeSoftKeyboard());
        onView(withId(R.id.characterText)).perform(scrollTo(), replaceText(chars), closeSoftKeyboard());
        onView(withId(R.id.locationText)).perform(scrollTo(), replaceText(loc), closeSoftKeyboard());

        onView(withId(R.id.addButton)).perform(scrollTo(), click());

        // After add, controller navigates to DreamDetail
        waitForView(UI_TIMEOUT_MS, R.id.dreamTitle);
        onView(withId(R.id.dreamTitle)).check(matches(isDisplayed()));
    }

    /** if auth is showing, sign in with test */
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

    // ---------------- wait helpers (same as your working test) ----------------

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

    private static ViewAction waitForDisplayed(long timeoutMs) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait up to " + timeoutMs + "ms";
            }

            @Override
            public void perform(UiController uiController, View root) {
                long end = System.currentTimeMillis() + timeoutMs;
                do {
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < end);
            }
        };
    }

    private static ViewAction waitForAnyMatch(Matcher<View> matcher, long timeoutMs) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for any view matching: " + matcher;
            }

            @Override
            public void perform(UiController uiController, View rootView) {
                long end = System.currentTimeMillis() + timeoutMs;

                do {
                    for (View v : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (matcher.matches(v) && v.isShown()) return;
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < end);

                throw new AssertionError("Timed out waiting for match: " + matcher);
            }
        };
    }
}
