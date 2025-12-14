package edu.vassar.cmpu203.dreamlog.view;

import androidx.annotation.NonNull;

import edu.vassar.cmpu203.dreamlog.model.Dream;
import edu.vassar.cmpu203.dreamlog.model.SharedDream;

public interface SharedDreamDetailUI {

    interface SharedDreamDetailListener {
        void onBackToSharedList();

        void onUnshareDream(@NonNull Dream dream);
    }

    void setListener(SharedDreamDetailListener listener);

    void displaySharedDream(@NonNull SharedDream sharedDream);
}