package edu.vassar.cmpu203.dreamlog.view;


import java.util.List;
import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * interface for the dream log viewing screen
 */
public interface ViewLogUI {


    /**
     * listener interface for view log events
     */
    interface ViewLogListener {
        /**
         * called when the view log screen is ready
         */
        void onViewLogReady(ViewLogUI ui);


        /**
         * called when user wants to filter dreams
         */
        void onFilter(String filterText);


        /**
         * called when user wants to clear the filter
         */
        void onClearFilter();


        /**
         * called when user wants to return to the main menu
         */
        void onBackToMenu();


        /**
         * called when user clicks on a dream to view details
         */
        void onDreamClicked(Dream dream, int position);
    }


    /**
     * sets the listener for view log events
     */
    void setListener(ViewLogListener viewLogListener);


    /**
     * updates the display with the list of dreams
     */
    void updateDreamDisplay(List<Dream> dreams);
}