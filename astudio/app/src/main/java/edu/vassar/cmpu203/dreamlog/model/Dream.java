package edu.vassar.cmpu203.dreamlog.model;


import androidx.annotation.NonNull;

import java.util.Collections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a dream that is part of the dream log.
 *
 * @param title the title of the dream
 * @param sum   the dream free text summary
 * @param themes the themes of the dream
 * @param chara  the characters in the dream
 * @param loc    the dream's location(s)
 */
public record Dream(String title, String sum, Set<String> themes, Set<String> chara, Set<String> loc) implements Serializable {

    private static final String TITLE_KEY = "title";
    private static final String SUMMARY_KEY = "summary";
    private static final String THEMES_KEY = "themes";
    private static final String CHARACTERS_KEY = "characters";
    private static final String LOCATIONS_KEY = "locations";


    /**
     * Creates a dream from text inputs, normalizing into a set
     *
     * @param title the title of the dream
     * @param sum   the dream free text summary
     * @param theme the comma-separated themes of the dream
     * @param chara the comma-separated characters in the dream
     * @param loc   the comma-separated locations for the dream
     * @return a Dream instance with parsed characters
     */
    public static Dream fromTextFields(String title, String sum, String theme, String chara, String loc) {
        Set<String> parsedThemes = parseTokens(theme);
        Set<String> parsedCharacters = parseTokens(chara);
        Set<String> parsedLocations = parseTokens(loc);
        return new Dream(title, sum, parsedThemes, parsedCharacters, parsedLocations);
    }

    /**
     * converts this Dream into a key-value map that can be stored in Firestore
     *
     * @return a map representation of this dream
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(TITLE_KEY, this.title);
        map.put(SUMMARY_KEY, this.sum);
        map.put(THEMES_KEY, new ArrayList<>(this.themes));
        map.put(CHARACTERS_KEY, new ArrayList<>(this.chara));
        map.put(LOCATIONS_KEY, new ArrayList<>(this.loc));
        return map;
    }

    /**
     * rebuilds a Dream from a key-value map
     *
     * @param map a map produced by ToMap()
     * @return a Dream containing the same information as the map
     */

    public static Dream fromMap(Map<String, Object> map) {
        String title = (String) map.getOrDefault(TITLE_KEY, "");
        String sum = (String) map.getOrDefault(SUMMARY_KEY, "");

        Set<String> themes = new LinkedHashSet<>();
        Object rawThemes = map.get(THEMES_KEY);
        if (rawThemes instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) rawThemes) {
                if (o != null) {
                    themes.add(o.toString());
                }
            }
        }

        Set<String> characters = new LinkedHashSet<>();
        Object rawCharacters = map.get(CHARACTERS_KEY);
        if (rawCharacters instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) rawCharacters) {
                if (o != null) {
                    characters.add(o.toString());
                }
            }
        }

        Set<String> locations = new LinkedHashSet<>();
        Object rawLocations = map.get(LOCATIONS_KEY);
        if (rawLocations instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) rawLocations) {
                if (o != null) {
                    locations.add(o.toString());
                }
            }
        }

        return new Dream(title, sum, themes, characters, locations);
    }


    /**
     * Provides a representation of the characters.
     *
     * @return joined character names
     */
    public String charactersAsText() {
        return joinTokens(this.chara);
    }


    /**
     * Provides a representation of the themes.
     *
     * @return joined theme names
     */
    public String themesAsText() {
        return joinTokens(this.themes);
    }


    /**
     * Provides a representation of the locations.
     *
     * @return joined location names
     */
    public String locationsAsText() {
        return joinTokens(this.loc);
    }


    /**
     * Returns a textual representation of the dream.
     *
     * @return the dream's textual representation
     */
    @NonNull
    @Override
    public String toString() {
        String themesString = joinTokens(this.themes);
        String charactersString = joinTokens(this.chara);
        String locationsString = joinTokens(this.loc);
        return String.format(
                "Dream{title='%s', summary='%s', themes='%s', characters='%s', locations='%s'}",
                this.title,
                this.sum,
                themesString,
                charactersString,
                locationsString
        );
    }


    /**
     * Returns an unmodifiable view of the dream's characters.
     *
     * @return immutable character set
     */
    @Override
    public Set<String> chara() {
        return Collections.unmodifiableSet(this.chara);
    }


    @Override
    public Set<String> themes() {
        return Collections.unmodifiableSet(this.themes);
    }


    @Override
    public Set<String> loc() {
        return Collections.unmodifiableSet(this.loc);
    }


    /**
     * Normalizes comma separated inputs into tokens
     */
    private static Set<String> parseTokens(String raw) {
        Set<String> parsed = new LinkedHashSet<>();
        for (String token : raw.split(",")) {
            String normalized = token.trim().toLowerCase();
            if (!normalized.isEmpty()) {
                parsed.add(normalized);
            }
        }
        return parsed;
    }


    private static String joinTokens(Set<String> tokens) {
        return tokens.stream()
                .sorted()
                .collect(Collectors.joining(", "));
    }
}