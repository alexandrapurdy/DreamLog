package edu.vassar.cmpu203.dreamlog.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.vassar.cmpu203.dreamlog.databinding.FragmentAuthBinding;

/**
 * Fragment that handles user sign in and registration.
 */
public class AuthFragment extends Fragment implements AuthUI {

    private FragmentAuthBinding binding;
    private AuthListener authListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentAuthBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.binding.signInButton.setOnClickListener(v -> attemptAuth(true));
        this.binding.registerButton.setOnClickListener(v -> attemptAuth(false));
    }

    private void attemptAuth(boolean signingIn) {
        if (this.authListener == null) {
            return;
        }
        String email = this.binding.emailInput.getText().toString().trim();
        String password = this.binding.passwordInput.getText().toString();
        String username = this.binding.usernameInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError(getString(edu.vassar.cmpu203.dreamlog.R.string.enter_email_password));
            return;
        }

        if (!signingIn && TextUtils.isEmpty(username)) {
            showError(getString(edu.vassar.cmpu203.dreamlog.R.string.enter_username));
            return;
        }

        showError("");
        if (signingIn) {
            this.authListener.onSignIn(email, password, username);
        } else {
            this.authListener.onRegister(email, password, username);
        }
    }

    @Override
    public void setListener(AuthListener authListener) {
        this.authListener = authListener;
    }

    @Override
    public void showLoading(boolean loading) {
        if (this.binding == null) {
            return;
        }
        this.binding.authProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        this.binding.signInButton.setEnabled(!loading);
        this.binding.registerButton.setEnabled(!loading);
    }

    @Override
    public void showError(@NonNull String message) {
        if (this.binding == null) {
            return;
        }
        if (message.isEmpty()) {
            this.binding.errorText.setVisibility(View.GONE);
        } else {
            this.binding.errorText.setVisibility(View.VISIBLE);
            this.binding.errorText.setText(message);
        }
    }
}