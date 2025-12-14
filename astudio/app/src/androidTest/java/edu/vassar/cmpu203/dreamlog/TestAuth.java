package edu.vassar.cmpu203.dreamlog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.test.espresso.NoMatchingViewException;

public final class TestAuth {
    public static final String TEST_USERNAME = "Test";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "123456";

    private TestAuth() {}

    /** true if a view with id is  displayed */
    public static boolean isDisplayedNow(@IdRes int viewId) {
        try {
            onView(withId(viewId)).check((v, noViewFoundException) -> {
                if (noViewFoundException != null) throw noViewFoundException;
                if (v.getVisibility() != View.VISIBLE) throw new AssertionError("Not visible");
            });
            return true;
        } catch (NoMatchingViewException | AssertionError e) {
            return false;
        }
    }

    /** wait until a view id is displayed  */
    public static void waitForDisplayed(@IdRes int viewId, long timeoutMs) {
        long start = SystemClock.elapsedRealtime();
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            if (isDisplayedNow(viewId)) return;
            SystemClock.sleep(100);
        }
        onView(withId(viewId)).check((v, e) -> {
            if (e != null) throw e;
        });
        onView(withId(viewId)).check((v, e) -> {}); // no-op
    }
}