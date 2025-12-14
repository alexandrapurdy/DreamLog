package edu.vassar.cmpu203.dreamlog.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.vassar.cmpu203.dreamlog.BuildConfig;

/**
 * makes analysis summaries for dreams and connects to Gemini for AI insights
 */
public class DreamAnalyzer {

    @Nullable
    private final GenerativeModelFutures generativeModel;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();

    public interface AiAnalysisCallback {
        void onAnalysisReady(@NonNull String aiText);
    }

    public DreamAnalyzer() {
        this(BuildConfig.GEMINI_API_KEY);
    }

    /**
     * creates an analyzer with the provided Gemini API key.
     *
     * @param apiKey Gemini API key
     */
    public DreamAnalyzer(@Nullable String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            GenerativeModel gm = new GenerativeModel(
                    "gemini-2.5-flash",
                    apiKey
            );
            this.generativeModel = GenerativeModelFutures.from(gm);
        } else {
            this.generativeModel = null;
        }
    }

    /**
     * generates a textual analysis for the provided dream
     *
     * @param dream the dream to analyze
     * @return an analysis summary describing dream attributes
     */
    public String generateAnalysis(final Dream dream) {
        StringBuilder analysis = new StringBuilder();

        analysis.append(" Dream Analysis\n\n");

        // theme analysis
        analysis.append("Themes: ").append(dream.themes().size()).append("\n");
        for (String theme : dream.themes()) {
            analysis.append("  • ").append(theme).append("\n");
        }
        analysis.append("\n");

        // character analysis
        analysis.append("Characters: ").append(dream.chara().size()).append("\n");
        for (String character : dream.chara()) {
            analysis.append("  • ").append(character).append("\n");
        }
        analysis.append("\n");

        // location analysis
        analysis.append("Locations: ").append(dream.loc().size()).append("\n");
        for (String location : dream.loc()) {
            analysis.append("  • ").append(location).append("\n");
        }
        analysis.append("\n");

        // summary analysis
        int wordCount = dream.sum().trim().isEmpty()
                ? 0
                : dream.sum().trim().split("\\s+").length;
        analysis.append("Summary Word Count: ").append(wordCount).append("\n\n");

        analysis.append(" AI Analysis Insights\n");

        return analysis.toString();
    }

    /**
     * Generate Gemini-backed analysis for the dream asynchronously.
     *
     * @param dream    the dream to analyze
     * @param callback analysis
     */
    public void generateAiAnalysis(@NonNull Dream dream, @NonNull AiAnalysisCallback callback) {
        if (this.generativeModel == null) {
            callback.onAnalysisReady("Add a GEMINI_API_KEY to local.properties to enable AI analysis.");
            return;
        }

        final String prompt = buildPrompt(dream);

        aiExecutor.submit(() -> {
            try {
                Content content = new Content.Builder()
                        .addText(prompt)
                        .build();

                java.util.concurrent.Future<GenerateContentResponse> future =
                        generativeModel.generateContent(content);

                GenerateContentResponse response = future.get();

                String text = response.getText();
                if (text == null || text.trim().isEmpty()) {
                    callback.onAnalysisReady("Gemini returned an empty response.");
                } else {
                    callback.onAnalysisReady(text.trim());
                }

            } catch (Exception e) {
                callback.onAnalysisReady("Gemini analysis failed: " + e.getMessage());
            }
        });
    }

    private String buildPrompt(@NonNull Dream dream) {
        return String.format(Locale.US,
                "You are an expert dream analyst. Provide a concise, symbolic-aware interpretation of the dream.\n\n" +
                        "Return ONLY 4–6 bullet points, each no more than 20 words.\n" +
                        "No paragraphs or headings.\n\n" +
                        "Guidelines:\n" +
                        "- Use tentative language such as may suggest, might symbolize, could reflect.\n" +
                        "- Do NOT add or invent details not in the dream.\n" +
                        "- You MAY reference common symbolic meanings (e.g. water, doors, light, animals) when clearly connected to the dream imagery.\n" +
                        "- Each bullet should connect a symbolic element to a possible emotional or psychological meaning.\n" +
                        "- Avoid vague clichés or overly broad interpretations.\n\n" +
                        "Dream data:\n" +
                        "Title: %s\n" +
                        "Summary: %s\n" +
                        "Themes: %s\n" +
                        "Characters: %s\n" +
                        "Locations: %s\n\n" +
                        "Output ONLY the bullet points.",
                dream.title(),
                dream.sum(),
                String.join(", ", dream.themes()),
                String.join(", ", dream.chara()),
                String.join(", ", dream.loc())
        );
    }

}