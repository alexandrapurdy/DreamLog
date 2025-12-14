package edu.vassar.cmpu203.dreamlog.view;

import androidx.annotation.NonNull;

/**
 * UI contract for authentication screens.
 */
public interface AuthUI {

    interface AuthListener {
        void onSignIn(@NonNull String email, @NonNull String password, @NonNull String username);
        void onRegister(@NonNull String email, @NonNull String password, @NonNull String username);
    }

    void setListener(AuthListener authListener);

    void showLoading(boolean loading);

    void showError(@NonNull String message);
}