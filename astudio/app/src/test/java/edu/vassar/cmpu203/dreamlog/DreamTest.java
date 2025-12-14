package edu.vassar.cmpu203.dreamlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.vassar.cmpu203.dreamlog.model.Dream;

/**
 * unit tests for dream
 */
public class DreamTest {

    /**
     * Verifies trimming, normalizing
     */
    @Test
    public void testFromText() {
        Dream dream = Dream.fromTextFields("Title", "Summary", " calm ,Peace , ,", " self,  ", " Home , garden  ");

        assertEquals(Set.of("calm", "peace"), dream.themes());
        assertEquals(Set.of("self"), dream.chara());
        assertEquals(Set.of("home", "garden"), dream.loc());
    }

    /**
     * map sterilization and reconstruction test
     */
    @Test
    public void testToMapPreserves() {
        Dream original = Dream.fromTextFields("A", "B", "x,y", "c,d", "l");

        Map<String, Object> map = original.toMap();
        Dream restored = Dream.fromMap(map);

        assertEquals(original.title(), restored.title());
        assertEquals(original.sum(), restored.sum());
        assertEquals(original.themes(), restored.themes());
        assertEquals(original.chara(), restored.chara());
        assertEquals(original.loc(), restored.loc());
    }

    /**
     * joined text helpers sort alphabetically and include all values
     */
    @Test
    public void testTokenJoinersSortConsistently() {
        Dream dream = new Dream("T", "S", Set.of("z", "a"), Set.of("b"), Set.of("o", "g"));

        assertEquals("a, z", dream.themesAsText());
        assertEquals("b", dream.charactersAsText());
        assertEquals("g, o", dream.locationsAsText());
    }

    /**
     *  that the accessor sets returned by the record are immutable views
     */
    @Test
    public void testAccessorSetsAreImmutable() {
        Dream dream = Dream.fromTextFields("T", "S", "a", "b", "c");

        assertThrows(UnsupportedOperationException.class, () -> dream.themes().add("new"));
        assertThrows(UnsupportedOperationException.class, () -> dream.chara().remove("b"));
        assertThrows(UnsupportedOperationException.class, () -> dream.loc().clear());
    }

    /**
     * checks the textual representation includes all parts of the dream and updates
     */
    @Test
    public void testToStringIncludesFields() {
        Dream dream = Dream.fromTextFields("Title", "Short summary", "theme", "hero", "bedroom");
        String text = dream.toString();

        assertTrue(text.contains("Title"));
        assertTrue(text.contains("Short summary"));
        assertTrue(text.contains("theme"));
        assertTrue(text.contains("hero"));
        assertTrue(text.contains("bedroom"));

        Dream different = Dream.fromTextFields("Title2", "Summary2", "theme", "hero", "bedroom");
        assertNotEquals(text, different.toString());
    }
}