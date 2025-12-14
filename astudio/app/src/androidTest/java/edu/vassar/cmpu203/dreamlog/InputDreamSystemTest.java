package edu.vassar.cmpu203.dreamlog;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

/**
 * system tests for Input Dream flow!!!
 *  - (if needed) login
 *  - Menu -> Input Dream
 *  - missing field validation
 *  - successful dream creation -> Dream Detail
 *  - Done button -> Menu
 */
@RunWith(AndroidJUnit4.class)
public class InputDreamSystemTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "123456";

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseAuth.getInstance().signOut();
    }

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

    /** returns true if a view with id is currently displayed. */
    private boolean isDisplayedNow(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** waits until either Auth or Menu is visible, then ensures we end on Menu. */
    private void ensureOnMenu() {
        long start = System.currentTimeMillis();
        long timeout = 12_000;

        // first wait until Auth or Menu shows up
        while (System.currentTimeMillis() - start < timeout) {
            if (isDisplayedNow(R.id.inputDreamButton)) {
                // already on Menu
                return;
            }
            if (isDisplayedNow(R.id.signInButton)) {
                // on Auth, login
                onView(withId(R.id.emailInput)).perform(replaceText(TEST_EMAIL), closeSoftKeyboard());
                onView(withId(R.id.passwordInput)).perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());
                onView(withId(R.id.signInButton)).perform(click());
                break;
            }
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }

        // Now wait for Menu after login attempt
        start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout) {
            if (isDisplayedNow(R.id.inputDreamButton)) return;
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }

        throw new AssertionError("Menu did not appear (either login failed or navigation didn't occur).");
    }

    private void goToInputDream() {
        onView(withId(R.id.inputDreamButton)).perform(click());
        onView(withId(R.id.addButton)).check(matches(isDisplayed()));
    }

    /**
     * missing fields should show validation error!
     */
    @Test
    public void inputDream_missingFields_showsError() {
        ensureOnMenu();
        goToInputDream();

        onView(withId(R.id.addButton)).perform(scrollTo(), click());

        onView(withText(R.string.missing_item_field_error))
                .check(matches(isDisplayed()));

        onView(withId(R.id.addButton)).check(matches(isDisplayed()));
    }

    /**
     * adding a valid dream should navigate to Dream Detail screen
     */
    @Test
    public void inputDream_validFields_navigatesToDetail() {
        ensureOnMenu();
        goToInputDream();

        onView(withId(R.id.dreamTitleText))
                .perform(scrollTo(), replaceText("Test Dream"), closeSoftKeyboard());
        onView(withId(R.id.summaryText))
                .perform(scrollTo(), replaceText("I was flying"), closeSoftKeyboard());
        onView(withId(R.id.themeText))
                .perform(scrollTo(), replaceText("freedom"), closeSoftKeyboard());
        onView(withId(R.id.characterText))
                .perform(scrollTo(), replaceText("me"), closeSoftKeyboard());
        onView(withId(R.id.locationText))
                .perform(scrollTo(), replaceText("sky"), closeSoftKeyboard());

        onView(withId(R.id.addButton)).perform(scrollTo(), click());

        onView(withId(R.id.dreamTitle))
                .check(matches(isDisplayed()))
                .check(matches(withText("Test Dream")));
    }

    /**
     * done button should return to menu
     */
    @Test
    public void inputDream_doneButton_returnsToMenu() {
        ensureOnMenu();
        goToInputDream();

        onView(withId(R.id.doneButton)).perform(scrollTo(), click());

        onView(withId(R.id.inputDreamButton)).check(matches(isDisplayed()));
        onView(withId(R.id.dreamLogButton)).check(matches(isDisplayed()));
    }
}