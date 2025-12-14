package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

@RunWith(AndroidJUnit4.class)
public class StartupSystemTest {

    private static final long UI_TIMEOUT_MS = 20_000;

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        FirebaseAuth.getInstance().signOut();

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5_000) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) break;
            sleep(50);
        }
    }

    @Test
    public void startup_showsAuthScreen() {
        try (ActivityScenario<?> scenario = ActivityScenario.launch(
                edu.vassar.cmpu203.dreamlog.controller.ControllerActivity.class)) {

            waitForViewInHierarchy(UI_TIMEOUT_MS, withId(R.id.signInButton));
            waitForViewInHierarchy(UI_TIMEOUT_MS, withId(R.id.registerButton));
            waitForViewInHierarchy(UI_TIMEOUT_MS, withId(R.id.emailInput));
            waitForViewInHierarchy(UI_TIMEOUT_MS, withId(R.id.passwordInput));
            waitForViewInHierarchy(UI_TIMEOUT_MS, withId(R.id.usernameInput));
        }
    }

    private static void waitForViewInHierarchy(long timeoutMs, Matcher<View> matcher) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (isViewPresentNow(matcher)) return;
            sleep(100);
        }
        throw new AssertionError("View not found in hierarchy: " + matcher);
    }

    private static boolean isViewPresentNow(Matcher<View> matcher) {
        final boolean[] found = {false};
        try {
            onView(isRoot()).perform(new ViewAction() {
                @Override public Matcher<View> getConstraints() { return isRoot(); }
                @Override public String getDescription() { return "scan view hierarchy"; }
                @Override public void perform(UiController uiController, View rootView) {
                    for (View v : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (matcher.matches(v)) { found[0] = true; return; }
                    }
                }
            });
        } catch (Throwable ignored) {}
        return found[0];
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
