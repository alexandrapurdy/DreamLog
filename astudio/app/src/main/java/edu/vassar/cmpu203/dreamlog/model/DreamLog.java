package edu.vassar.cmpu203.dreamlog.model;


import androidx.annotation.NonNull;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Represents a dream log, which aggregates dreams
 */
public class DreamLog implements Serializable {


    private static final String DREAMS_KEY = "dreams";


    private final Map<Integer, Dream> dreams;


    public DreamLog() {
        this.dreams = new LinkedHashMap<>();
    }


    public Map<Integer, Dream> getDreams() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(dreams));
    }


    /**
     * provides the dreams in newest-first order for display
     */
    public List<Dream> getDreamList() {
        List<Entry<Integer, Dream>> entries = new ArrayList<>(dreams.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getKey(), a.getKey())); // NEWEST FIRST


        List<Dream> orderedDreams = new ArrayList<>();
        for (Entry<Integer, Dream> entry : entries) {
            orderedDreams.add(entry.getValue());
        }
        return orderedDreams;
    }


    /**
     * Adds a dream object to the dream log
     */
    public int addDream(final Dream dr) {
        int i = 0;
        boolean adding = true;
        while (adding) {
            if (!dreams.containsKey(i)) {
                dreams.put(i, dr);
                adding = false;
            } else {
                i++;
            }
        }
        return i;
    }


    public List<Dream> filterDreams(String filterText) {
        String normalized = filterText.toLowerCase().trim();
        if (normalized.isEmpty()) {
            return new ArrayList<>(getDreamList());
        }


        List<Dream> allDreams = getDreamList();
        List<Dream> filteredDreams = new ArrayList<>();
        for (Dream dream : allDreams) {
            boolean matches = dream.title().toLowerCase().contains(normalized)
                    || dream.sum().toLowerCase().contains(normalized);


            if (!matches) {
                matches = dream.themes().stream().anyMatch(theme -> theme.contains(normalized));
            }


            if (!matches) {
                for (String character : dream.chara()) {
                    if (character.toLowerCase().contains(normalized)) {
                        matches = true;
                        break;
                    }
                }
            }


            if (!matches) {
                matches = dream.loc().stream().anyMatch(location -> location.contains(normalized));
            }


            if (matches) {
                filteredDreams.add(dream);
            }
        }
        return filteredDreams;
    }


    public boolean updateDream(final int index, final Dream updatedDream) {
        if (dreams.containsKey(index)) {
            dreams.put(index, updatedDream);
            return true;
        }
        return false;
    }


    public boolean deleteDream(final int index) {
        if (dreams.containsKey(index)) {
            dreams.remove(index);
            return true;
        }
        return false;
    }


    public void clear() {
        this.dreams.clear();
    }


    public int findDreamIndex(Dream dream) {
        for (Entry<Integer, Dream> entry : dreams.entrySet()) {
            if (entry.getValue().equals(dream)) {
                return entry.getKey();
            }
        }
        return -1;
    }


    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();


        List<Map<String, Object>> dreamMaps = new ArrayList<>();
        for (Dream dream : getDreamList()) {
            dreamMaps.add(dream.toMap());
        }
        map.put(DREAMS_KEY, dreamMaps);


        return map;
    }


    @NonNull
    public static DreamLog fromMap(Map<String, Object> map) {
        final DreamLog dreamLog = new DreamLog();


        Object rawDreams = map.get(DREAMS_KEY);
        if (rawDreams instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) rawDreams) {
                if (o instanceof Map<?, ?> dreamMapRaw) {
                    Map<String, Object> dreamMap = new HashMap<>();
                    dreamMapRaw.forEach((k, v) -> dreamMap.put(String.valueOf(k), v));
                    Dream dream = Dream.fromMap(dreamMap);
                    dreamLog.addDream(dream);
                }
            }
        }


        return dreamLog;
    }


    @NonNull
    @Override
    public String toString() {
        if (dreams.isEmpty()) {
            return "dream log is empty!!";
        }


        StringBuilder sb = new StringBuilder();
        for (Dream dream : getDreamList()) {
            sb.append(dream.toString());
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}





