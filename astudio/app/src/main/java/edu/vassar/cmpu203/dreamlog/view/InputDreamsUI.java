package edu.vassar.cmpu203.dreamlog.view;

public interface InputDreamsUI {

    interface InputDreamsListener {

        /**
         * Called when the user wants to add a new dream
         */
        void onAddDream(final String title, final String sum, final String theme, final String chara,
                        final String loc);

        /**
         * called when the user has finished adding dreams to the dream log
         */
        void onDoneAddingDream();
    }

    /**
     * sets the listener for menu events
     */
    void setListener(final InputDreamsListener inputDreamsListener);

}

