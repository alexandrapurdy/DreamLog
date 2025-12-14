package edu.vassar.cmpu203.dreamlog;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.vassar.cmpu203.dreamlog.model.Dream;
import edu.vassar.cmpu203.dreamlog.model.DreamAnalyzer;

/**
 * Unit tests for dreamanalyzer
 */
public class DreamAnalyzerTest {

    private final DreamAnalyzer analyzer = new DreamAnalyzer();

    /**
     * makes sure that multiple tokens are counted correctly and listed in the output with expected headings
     */
    @Test
    public void testGenerateAnalysisWithMultipleValues() {
        Dream dream = Dream.fromTextFields(
                "T",
                "this is a great summary",
                "fear, adventure, mystery",
                "hero, villain",
                "forest, cave");

        String out = analyzer.generateAnalysis(dream);

        assertTrue(out.contains("Themes: 3"));
        assertTrue(out.contains("• fear"));
        assertTrue(out.contains("• adventure"));
        assertTrue(out.contains("• mystery"));

        assertTrue(out.contains("Characters: 2"));
        assertTrue(out.contains("• hero"));
        assertTrue(out.contains("• villain"));

        assertTrue(out.contains("Locations: 2"));
        assertTrue(out.contains("• forest"));
        assertTrue(out.contains("• cave"));

        assertTrue(out.contains("Summary Word Count: 5"));
        assertTrue(out.contains("AI Analysis"));
    }

    /**
     * makes sure that the analysis handles empty fields and still produces all sections
     */
    @Test
    public void testGenerateAnalysisWithEmptyValues() {
        Dream emptyDream = Dream.fromTextFields("", "singleword", "", "", "");

        String out = analyzer.generateAnalysis(emptyDream);

        assertTrue(out.contains("Themes: 0"));
        assertTrue(out.contains("Characters: 0"));
        assertTrue(out.contains("Locations: 0"));
        assertTrue(out.contains("Summary Word Count: 1"));
    }
}