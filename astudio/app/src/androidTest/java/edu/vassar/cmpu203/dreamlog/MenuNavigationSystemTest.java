package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

/**
 * system tests for menu navigation
 */
@RunWith(AndroidJUnit4.class)
public class MenuNavigationSystemTest {

    private ActivityScenario<ControllerActivity> scenario;

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        scenario = ActivityScenario.launch(ControllerActivity.class);

        signInIfNeeded();

        waitForView(R.id.inputDreamButton);
    }

    @After
    public void tearDown() {
        if (scenario != null) scenario.close();
    }

    /** Input Dream button navigates to Input Dream screen */
    @Test
    public void menu_inputDreamButton_navigates() {
        onView(withId(R.id.inputDreamButton)).perform(click());
        onView(withId(R.id.dreamTitleText)).check(matches(isDisplayed()));
    }

    /** View DreamLog button navigates to log */
    @Test
    public void menu_viewLogButton_navigates() {
        onView(withId(R.id.dreamLogButton)).perform(click());
        onView(withId(R.id.dreamsRecyclerView)).check(matches(isDisplayed()));
    }

    /** Shared Dreams button navigates to shared list */
    @Test
    public void menu_sharedDreamsButton_navigates() {
        onView(withId(R.id.sharedDreamsButton)).perform(click());
        onView(withId(R.id.sharedDreamsTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.sharedDreamsRecycler)).check(matches(isDisplayed()));
    }

    /**
     * signs in if the auth screen is showing
     */
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
            // already signed in / already on menu
        }
    }

    /**
     * wait for view
     */
    private void waitForView(int viewId) {
        onView(isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for view with id " + viewId;
            }

            @Override
            public void perform(UiController uiController, View rootView) {
                long timeout = System.currentTimeMillis() + 10_000;

                do {
                    for (View v : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (v.getId() == viewId) return;
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < timeout);

                throw new AssertionError("View not found: " + viewId);
            }
        });
    }
}
