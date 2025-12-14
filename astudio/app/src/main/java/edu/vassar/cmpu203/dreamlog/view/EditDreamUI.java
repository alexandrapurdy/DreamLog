package edu.vassar.cmpu203.dreamlog.view;


import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * Interface for the edit dream screen
 */
public interface EditDreamUI {


    /**
     * Listener interface for edit dream events
     */
    interface EditDreamListener {
        /**
         * called when the edit screen is ready to show dream information
         */
        void onEditDreamReady(EditDreamUI ui);

        /**
         * called when user saves the edited dream
         */
        void onSaveEdit(String title, String sum, String theme, String chara, String loc,
                        int dreamIndex);


        /**
         * called when user cancels editing
         */
        void onCancelEdit();
    }


    /**
     * sets the listener for edit dream events
     */
    void setListener(EditDreamListener editDreamListener);


    /**
     * populates the edit fields with an existing dream
     */
    void displayDreamForEdit(Dream dream, int dreamIndex);
}