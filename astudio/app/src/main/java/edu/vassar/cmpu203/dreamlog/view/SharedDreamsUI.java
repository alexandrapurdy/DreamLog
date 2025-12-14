package edu.vassar.cmpu203.dreamlog.view;

import androidx.annotation.NonNull;

import java.util.List;

import edu.vassar.cmpu203.dreamlog.model.SharedDream;

/**
 * UI contract for browsing shared dreams.
 */
public interface SharedDreamsUI {

    interface SharedDreamsListener {
        void onLoadSharedDreams(String usernameFilter);
        void onBackToMenu();
        void onSharedDreamSelected(@NonNull SharedDream sharedDream);
    }

    void setListener(SharedDreamsListener listener);

    void showLoading(boolean loading);

    void showError(@NonNull String message);

    void updateDreamDisplay(@NonNull List<SharedDream> dreams);
}