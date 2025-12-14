package edu.vassar.cmpu203.dreamlog.view;

import androidx.annotation.NonNull;


/**
 * interface for the main menu screen.
 */
public interface MenuUI {


    /**
     * listener interface for menu events
     */
    interface MenuListener {
        /**
         * called when user wants to input a new dream.
         */
        void onInputDream();


        /**
         * called when user wants to view their dream log
         */
        void onViewLog();

        /**
         * called when user wants to see shared dreams.
         */
        void onViewSharedDreams();

        /**
         * called when user wants to sign out.
         */
        void onSignOut();

        /**
         * called when the menu view is ready for display.
         */
        void onMenuReady(MenuUI ui);
    }


    /**
     * sets the listener for menu events
     */
    void setListener(MenuListener menuListener);

    /**
     * displays the authenticated user's username.
     */
    void showUsername(@NonNull String username);

    /**
     * displays a message indicating sign-in is required.
     */
    void showSignedOut();
}