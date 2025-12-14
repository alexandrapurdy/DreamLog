package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.initializeAndSignOut;
import static edu.vassar.cmpu203.dreamlog.SystemTestUtils.waitForView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.vassar.cmpu203.dreamlog.controller.ControllerActivity;

@RunWith(AndroidJUnit4.class)
public class AuthSystemTest {

    @Before
    public void setUp() {
        initializeAndSignOut();
    }

    @Rule
    public ActivityScenarioRule<ControllerActivity> scenarioRule =
            new ActivityScenarioRule<>(ControllerActivity.class);


    /** if signing in and info is missing, the auth screen should show an error */
    @Test
    public void signInMissingInfo() {
        waitForView(R.id.signInButton);

        onView(withId(R.id.signInButton)).perform(click());

        onView(withId(R.id.errorText)).check(matches(isDisplayed()));
        onView(withId(R.id.errorText)).check(matches(withText(R.string.enter_email_password)));
    }

    /** if registering and username is missing, the auth screen should show an error */
    @Test
    public void registerMissingUsername() {
        waitForView(R.id.registerButton);
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));

        onView(withId(R.id.emailInput)).perform(replaceText("idk@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.usernameInput)).perform(click(), clearText(), closeSoftKeyboard());

        onView(withId(R.id.registerButton)).perform(click());

        onView(withId(R.id.errorText)).check(matches(isDisplayed()));
        onView(withId(R.id.errorText)).check(matches(withText(R.string.enter_username)));
    }

    /** logs in with a test account and confirms the Menu shows signed-in status */
    @Test
    public void signInWorks() {
        final String TEST_EMAIL = "test@example.com";
        final String TEST_PASSWORD = "123456";

        waitForView(R.id.emailInput);

        onView(withId(R.id.emailInput)).perform(replaceText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());

        waitForView(R.id.userStatus);

        onView(withId(R.id.userStatus)).check(matches(isDisplayed()));
        onView(withId(R.id.userStatus))
                .check(matches(withText(org.hamcrest.Matchers.containsString("Signed in as"))));
    }

}



