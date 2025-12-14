package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.ensureOnMenu;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

/**
 * system tests for menu navigation
 */
@RunWith(AndroidJUnit4.class)
public class MenuNavigationSystemTest {

    private ActivityScenario<ControllerActivity> scenario;

    @Before
    public void setUp() {
        initializeAndSignOut();
        scenario = ActivityScenario.launch(ControllerActivity.class);
        ensureOnMenu();
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

}
