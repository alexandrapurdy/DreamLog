package edu.vassar.cmpu203.dreamlog.view;


import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * interface for the dream detail screen
 */
public interface DreamDetailUI {

    /**
     * listener interface for dream detail events
     */
    interface DreamDetailListener {
        /**
         * called when the dream detail screen is ready to display information
         */
        void onDreamDetailReady(DreamDetailUI ui);

        /**
         * called when user wants to edit the dream
         */
        void onEditDream(Dream dream, int dreamIndex);


        /**
         * called when user wants to delete the dream
         */
        void onDeleteDream(int dreamIndex);

        /**
         * called when user wants to share the dream
         */
        void onShareDream(Dream dream);

        /**
         * called when user wants to go back to the log
         */
        void onBackToLog();
    }


    /**
     * sets the listener for dream detail events
     */
    void setListener(DreamDetailListener dreamDetailListener);


    /**
     * displays the dream details
     */
    void displayDream(Dream dream, int dreamIndex, String analysis);


    /**
    * updates the analysis text
    */
    void updateAnalysis(String analysis);

}

