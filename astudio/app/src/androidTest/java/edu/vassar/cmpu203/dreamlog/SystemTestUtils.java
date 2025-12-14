package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;

/**
 * Shared helpers for instrumentation system tests to reduce duplication and improve stability.
 */
public final class SystemTestUtils {

    private static final long DEFAULT_TIMEOUT_MS = 10_000L;
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PASSWORD = "123456";

    private SystemTestUtils() {}

    /** Initializes Firebase and signs out to ensure a clean auth state. */
    public static void initializeAndSignOut() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseAuth.getInstance().signOut();
    }

    /** Waits until the provided view id becomes visible or throws after timeout. */
    public static void waitForView(int viewId) {
        onView(isRoot()).perform(waitForViewAction(DEFAULT_TIMEOUT_MS, viewId));
    }

    /** Waits until any of the given ids is visible or throws after timeout. */
    public static void waitForAnyView(int... viewIds) {
        onView(isRoot()).perform(waitForAnyViewAction(DEFAULT_TIMEOUT_MS, viewIds));
    }

    /** Attempts to sign in through the UI if the auth screen is showing. */
    public static void signInIfNeeded() {
        try {
            onView(withId(R.id.authTitle)).check((view, noView) -> {});
        } catch (Throwable ignored) {
            return; // already past auth
        }

        onView(withId(R.id.emailInput)).perform(click(), replaceText(DEFAULT_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(click(), replaceText(DEFAULT_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
    }

    /** Ensures the app is on the menu screen, signing in first if necessary. */
    public static void ensureOnMenu() {
        waitForAnyView(R.id.authTitle, R.id.inputDreamButton);
        signInIfNeeded();
        waitForView(R.id.inputDreamButton);
        waitForView(R.id.dreamLogButton);
        waitForView(R.id.sharedDreamsButton);
    }

    /** Returns whether a view is currently displayed, swallowing assertion errors. */
    public static boolean isDisplayedNow(int viewId) {
        try {
            onView(withId(viewId)).check((view, noViewFoundException) -> {
                if (!view.isShown()) throw new AssertionError();
            });
            return true;
        } catch (Throwable t) {
            return false;
        }
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
                        if (v.getId() == viewId && v.isShown()) return;
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
                            if (vid == target && v.isShown()) return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < end);

                throw new AssertionError("Timed out waiting for any of the provided view ids.");
            }
        };
    }
}
