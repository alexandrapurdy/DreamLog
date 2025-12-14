package edu.vassar.cmpu203.dreamlog;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.ensureOnMenu;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;

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

    @Before
    public void setUp() {
        initializeAndSignOut();
    }

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);

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