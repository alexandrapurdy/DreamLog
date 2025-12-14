package edu.vassar.cmpu203.dreamlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.vassar.cmpu203.dreamlog.model.Dream;
import edu.vassar.cmpu203.dreamlog.model.DreamLog;

/**
 * unit tests for dreamlog
 */
public class DreamLogTest {

    private DreamLog log;

    @Before
    public void setup() {
        log = new DreamLog();
    }

    /**
     * dreams are added in the lowest available slot and ordering is preserved
     */
    @Test
    public void testAddDreamMaintainsIndexes() {
        Dream first = Dream.fromTextFields("T1", "S1", "theme1", "a", "l1");
        Dream second = Dream.fromTextFields("T2", "S2", "theme2", "b", "l2");

        int idx1 = log.addDream(first);
        int idx2 = log.addDream(second);

        assertEquals(0, idx1);
        assertEquals(1, idx2);
        assertEquals(List.of(first, second), log.getDreamList());
    }

    /**
     * removed indexes are reused when adding dreams
     */
    @Test
    public void testAddDreamReusesLowestIndex() {
        log.addDream(Dream.fromTextFields("First", "S1", "th1", "A", "L1"));
        log.addDream(Dream.fromTextFields("Second", "S2", "th2", "B", "L2"));

        assertTrue(log.deleteDream(0));

        Dream replacement = Dream.fromTextFields("Replacement", "S3", "th3", "C", "L3");
        int replacementIndex = log.addDream(replacement);

        assertEquals(0, replacementIndex);
        assertEquals(replacement, log.getDreams().get(0));
    }

    /**
     * filtering matches across title, summary, themes, characters and locations
     */
    @Test
    public void testFilterDreamsMatchesDifferentFields() {
        log.addDream(Dream.fromTextFields("Calm Night", "Quiet place", "peace", "me", "home"));
        log.addDream(Dream.fromTextFields("Adventure", "Forest run", "explore", "runner", "woods"));

        List<Dream> byTitle = log.filterDreams("calm");
        assertEquals(1, byTitle.size());
        assertEquals("Calm Night", byTitle.get(0).title());

        List<Dream> bySummary = log.filterDreams("FOREST");
        assertEquals(1, bySummary.size());
        assertEquals("Adventure", bySummary.get(0).title());

        List<Dream> byTheme = log.filterDreams("peace");
        assertEquals("Calm Night", byTheme.get(0).title());

        List<Dream> byCharacter = log.filterDreams("runner");
        assertEquals("Adventure", byCharacter.get(0).title());

        List<Dream> byLocation = log.filterDreams("home");
        assertEquals("Calm Night", byLocation.get(0).title());
    }

    /**
     * empty filter returns all dreams
     */
    @Test
    public void testFilterDreamsWithEmptyQueryReturnsAll() {
        Dream first = Dream.fromTextFields("Calm Night", "Quiet place", "peace", "me", "home");
        Dream second = Dream.fromTextFields("Adventure", "Forest run", "explore", "runner", "woods");
        log.addDream(first);
        log.addDream(second);

        List<Dream> filtered = log.filterDreams("   ");

        assertEquals(List.of(first, second), filtered);
        assertFalse(filtered == log.getDreamList());
    }

    /**
     * clearing the log works
     */
    @Test
    public void testClearResetsStateAndIndexes() {
        log.addDream(Dream.fromTextFields("First", "S", "th", "c", "l"));
        log.addDream(Dream.fromTextFields("Second", "S2", "th2", "c2", "l2"));

        log.clear();

        assertTrue(log.getDreams().isEmpty());

        int newIndex = log.addDream(Dream.fromTextFields("Third", "S3", "th3", "c3", "l3"));
        assertEquals(0, newIndex);
        assertEquals(List.of("Third"), log.getDreamList().stream().map(Dream::title).toList());
    }

    /**
     * checks that updates succeed
     */
    @Test
    public void testUpdateDreamValidatesIndexes() {
        log.addDream(Dream.fromTextFields("Old", "S", "th", "C", "L"));

        Dream updated = Dream.fromTextFields("New", "S2", "th2", "C2", "L2");
        assertTrue(log.updateDream(0, updated));
        assertEquals(updated, log.getDreams().get(0));

        assertFalse(log.updateDream(99, updated));
    }

    /**
     * confirms that deletion removes items
     */
    @Test
    public void testDeleteDreamAndFindIndex() {
        Dream dream = Dream.fromTextFields("x", "y", "z", "c", "l");
        log.addDream(dream);

        assertEquals(0, log.findDreamIndex(dream));
        assertTrue(log.deleteDream(0));
        assertEquals(-1, log.findDreamIndex(dream));
        assertFalse(log.deleteDream(0));
    }


}