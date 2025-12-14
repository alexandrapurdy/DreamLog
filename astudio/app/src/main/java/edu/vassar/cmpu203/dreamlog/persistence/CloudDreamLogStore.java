package edu.vassar.cmpu203.dreamlog.persistence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.vassar.cmpu203.dreamlog.model.DreamLog;

/**
 * Persists each user's dream log to Firestore so entries are available across devices.
 */
public class CloudDreamLogStore {

    private static final String COLLECTION = "userDreamLogs";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    /** Callback for dream log loads. */
    public interface LoadCallback {
        void onLoaded(@Nullable DreamLog dreamLog);

        void onError(@NonNull String message);
    }

    /** Callback for dream log saves. */
    public interface SaveCallback {
        void onSaved();

        void onError(@NonNull String message);
    }

    /**
     * Loads the dream log associated with the provided username.
     */
    public void loadDreamLog(@NonNull String username, @NonNull LoadCallback callback) {
        DocumentReference documentReference = firestore.collection(COLLECTION).document(username);
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        DreamLog dreamLog = DreamLog.fromMap(documentSnapshot.getData());
                        callback.onLoaded(dreamLog);
                    } else {
                        callback.onLoaded(null);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() == null ? "Unable to load dream log" : e.getMessage()));
    }

    /**
     * Saves the dream log under the provided username document.
     */
    public void saveDreamLog(@NonNull String username, @NonNull DreamLog dreamLog, @NonNull SaveCallback callback) {
        firestore.collection(COLLECTION)
                .document(username)
                .set(dreamLog.toMap())
                .addOnSuccessListener(unused -> callback.onSaved())
                .addOnFailureListener(e -> callback.onError(e.getMessage() == null ? "Unable to save dream log" : e.getMessage()));
    }
}