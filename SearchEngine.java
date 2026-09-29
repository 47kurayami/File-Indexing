import java.util.*;

public class SearchEngine {
    private Map<String, Map<String, Integer>> index;

    public SearchEngine(Map<String, Map<String, Integer>> index) {
        this.index = index;
    }

    // returns files ranked by total keyword frequency, highest first
    public List<Map.Entry<String, Integer>> search(String query) {
        Map<String, Integer> scores = new HashMap<>();

        for (String word : Tokenizer.tokenize(query)) {
            Map<String, Integer> fileCounts = index.get(word);
            if (fileCounts == null) continue;   // word not in any file

            for (Map.Entry<String, Integer> e : fileCounts.entrySet()) {
                scores.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }

        List<Map.Entry<String, Integer>> results = new ArrayList<>(scores.entrySet());
        Collections.sort(results, (a, b) -> b.getValue() - a.getValue());
        return results;
    }   // <- first method ends here

    // same search, but only keeps files that are in the given collection
    public List<Map.Entry<String, Integer>> search(String query, Set<String> allowedFiles) {
        List<Map.Entry<String, Integer>> results = search(query);
        if (allowedFiles != null) {
            results.removeIf(e -> !allowedFiles.contains(e.getKey()));
        }
        return results;
    }
}