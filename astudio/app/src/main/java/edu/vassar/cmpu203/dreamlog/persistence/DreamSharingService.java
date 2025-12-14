package edu.vassar.cmpu203.dreamlog.persistence;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import edu.vassar.cmpu203.dreamlog.model.Dream;
import edu.vassar.cmpu203.dreamlog.model.SharedDream;


/**
 * Handles sharing dreams to Firestore and retrieving shared dreams from other users.
 */
public class DreamSharingService {


    private static final String COLLECTION = "sharedDreams";
    private final CollectionReference sharedDreamsRef = FirebaseFirestore.getInstance().collection(COLLECTION);


    /** Callback for share operations. */
    public interface ShareListener {
        void onShareSucceeded();
        void onShareFailed(@NonNull String message);
    }


    /** Callback for unshare operations. */
    public interface UnshareListener {
        void onUnshareSucceeded();
        void onUnshareFailed(@NonNull String message);
    }


    /** Callback for loading shared dreams. */
    public interface LoadListener {
        void onDreamsLoaded(@NonNull List<SharedDream> dreams);
        void onLoadFailed(@NonNull String message);
    }


    /**
     * Shares the provided dream to Firestore under the authenticated user's account.
     */
    public void shareDream(@NonNull FirebaseUser user, @NonNull Dream dream, @NonNull ShareListener listener) {
        SharedDream sharedDream = new SharedDream(dream, extractUsername(user), user.getUid(), new Date());


        this.sharedDreamsRef
                .document(docId(user, dream))
                .set(sharedDream.toMap())
                .addOnSuccessListener(v -> listener.onShareSucceeded())
                .addOnFailureListener(e -> listener.onShareFailed(e.getMessage() == null ? "Share failed" : e.getMessage()));
    }


    /**
     * loads shared dreams. If a username filter is provided, returns only dreams shared by that user
     */
    public void loadSharedDreams(@Nullable String ownerUsername, @NonNull LoadListener listener) {
        Query query;
        String trimmedUsername = ownerUsername == null ? null : ownerUsername.trim();


        if (trimmedUsername != null && !trimmedUsername.isEmpty()) {
            query = this.sharedDreamsRef.whereEqualTo(SharedDream.OWNER_USERNAME_KEY, trimmedUsername).limit(50);
        } else {
            query = this.sharedDreamsRef.orderBy(SharedDream.TIMESTAMP_KEY, Query.Direction.DESCENDING).limit(50);
        }


        query.get()
                .addOnSuccessListener(qsnap -> {
                    List<SharedDream> dreams = new ArrayList<>();
                    qsnap.forEach(doc -> dreams.add(SharedDream.fromSnapshot(doc)));


                    if (trimmedUsername != null && !trimmedUsername.isEmpty()) {
                        dreams.sort((a, b) -> b.timestamp().compareTo(a.timestamp()));
                    }
                    listener.onDreamsLoaded(dreams);
                })
                .addOnFailureListener(e -> listener.onLoadFailed(e.getMessage() == null ? "Unable to load shared dreams" : e.getMessage()));
    }


    /**
     * removes the shared instance of a dream for the authenticated owner, without deleting the dream log entry
     */
    public void unshareDream(@NonNull FirebaseUser user, @NonNull Dream dream, @NonNull UnshareListener listener) {
        this.sharedDreamsRef
                .document(docId(user, dream))
                .delete()
                .addOnSuccessListener(v -> listener.onUnshareSucceeded())
                .addOnFailureListener(e -> listener.onUnshareFailed(e.getMessage() == null ? "Unable to unshare dream" : e.getMessage()));
    }


    /**
     * document id so a dream can't be shared multiple times
     */
    private String docId(@NonNull FirebaseUser user, @NonNull Dream dream) {
        String key = normalize(dream.title()) + "|" +
                normalize(dream.sum()) + "|" +
                dream.themesAsText() + "|" +
                dream.charactersAsText() + "|" +
                dream.locationsAsText();

        return user.getUid() + "_" + key.hashCode();
    }


    private String normalize(@Nullable String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }


    private String extractUsername(@NonNull FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        if (user.getEmail() != null && user.getEmail().contains("@")) {
            return user.getEmail().substring(0, user.getEmail().indexOf('@'));
        }
        return user.getUid();
    }


}