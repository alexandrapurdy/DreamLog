package edu.vassar.cmpu203.dreamlog.model;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a dream that has been shared to the cloud along with owner data
 */
public class SharedDream {
    public static final String OWNER_USERNAME_KEY = "ownerUsername";
    public static final String OWNER_ID_KEY = "ownerId";
    public static final String DREAM_KEY = "dream";
    public static final String TIMESTAMP_KEY = "timestamp";

    private final Dream dream;
    private final String ownerUsername;
    private final String ownerId;
    private final Date timestamp;
    private final String documentId;

    public SharedDream(@NonNull Dream dream, @NonNull String ownerUsername, @NonNull String ownerId, @NonNull Date timestamp) {
        this(dream, ownerUsername, ownerId, timestamp, "");
    }

    public SharedDream(@NonNull Dream dream, @NonNull String ownerUsername, @NonNull String ownerId, @NonNull Date timestamp, @NonNull String documentId) {
        this.dream = dream;
        this.ownerUsername = ownerUsername;
        this.ownerId = ownerId;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    public Dream dream() {
        return this.dream;
    }

    public String ownerUsername() {
        return this.ownerUsername;
    }

    public String ownerId() {
        return this.ownerId;
    }

    public Date timestamp() {
        return this.timestamp;
    }

    public String documentId() {
        return this.documentId;
    }

    /**
     * Converts the shared dream to a map for Firestore persistence
     */
    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(OWNER_USERNAME_KEY, this.ownerUsername);
        map.put(OWNER_ID_KEY, this.ownerId);
        map.put(DREAM_KEY, this.dream.toMap());
        map.put(TIMESTAMP_KEY, this.timestamp);
        return map;
    }

    /**
     * Reconstructs a SharedDream from a key-value map
     */
    @NonNull
    public static SharedDream fromMap(@NonNull Map<String, Object> map) {
        Object dreamRaw = map.get(DREAM_KEY);
        Map<String, Object> dreamMap = new HashMap<>();
        if (dreamRaw instanceof Map<?, ?> rawMap) {
            rawMap.forEach((k, v) -> dreamMap.put(String.valueOf(k), v));
        }
        Dream dream = Dream.fromMap(dreamMap);

        String ownerUsername = String.valueOf(map.getOrDefault(OWNER_USERNAME_KEY, "unknown"));
        String ownerId = String.valueOf(map.getOrDefault(OWNER_ID_KEY, ""));
        Date timestamp = new Date();
        Object tsObj = map.get(TIMESTAMP_KEY);
        if (tsObj instanceof Timestamp ts) {
            timestamp = ts.toDate();
        } else if (tsObj instanceof Date date) {
            timestamp = date;
        }
        return new SharedDream(dream, ownerUsername, ownerId, timestamp);
    }

    /**
     * Reconstructs a SharedDream from a document snapshot and retains its id for unsharing
     */
    @NonNull
    public static SharedDream fromSnapshot(@NonNull DocumentSnapshot snapshot) {
        Map<String, Object> data = snapshot.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        SharedDream sharedDream = fromMap(data);
        return new SharedDream(sharedDream.dream(), sharedDream.ownerUsername(), sharedDream.ownerId(), sharedDream.timestamp(), snapshot.getId());
    }
}