package edu.vassar.cmpu203.dreamlog.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.vassar.cmpu203.dreamlog.model.DreamLog;

/**
 * Persists each user's dream log locally so it survives sign-outs and app restarts.
 */
public class UserDreamStorage {

    private static final String STORAGE_FNAME = "user_dream_logs.ser";

    private final File file;

    public UserDreamStorage(@NonNull Context context) {
        this.file = new File(context.getFilesDir(), STORAGE_FNAME);
    }

    public synchronized void saveDreamLog(@NonNull String username, @NonNull DreamLog dreamLog) {
        Map<String, DreamLog> allLogs = readAllLogs();
        allLogs.put(username, dreamLog);

        try (ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(this.file))) {
            ooStream.writeObject(allLogs);
        } catch (IOException e) {
            Log.e("DreamLog", "Unable to save dream log", e);
        }
    }

    @Nullable
    public synchronized DreamLog loadDreamLog(@NonNull String username) {
        Map<String, DreamLog> allLogs = readAllLogs();
        return allLogs.get(username);
    }

    @NonNull
    private Map<String, DreamLog> readAllLogs() {
        if (!this.file.isFile()) {
            return new HashMap<>();
        }

        try (ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(this.file))) {
            Object raw = oiStream.readObject();
            if (raw instanceof Map<?, ?> rawMap) {
                Map<String, DreamLog> converted = new HashMap<>();
                rawMap.forEach((k, v) -> {
                    if (k != null && v instanceof DreamLog dreamLog) {
                        converted.put(String.valueOf(k), dreamLog);
                    }
                });
                return converted;
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e("DreamLog", "Unable to load stored dream logs", e);
        }

        return new HashMap<>();
    }
}
