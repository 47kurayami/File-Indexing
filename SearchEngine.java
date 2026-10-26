import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchEngine {
    public record Hit(String file, int score) {
    }

    private final Map<String, Map<String, Integer>> index;

    public SearchEngine(Map<String, Map<String, Integer>> index) {
        this.index = index;
    }

    public List<Hit> search(String query, Set<String> allowed) {
        Map<String, Integer> scores = new HashMap<>();

        for (String word : Tokenizer.tokenize(query)) {
            Map<String, Integer> counts = index.get(word);
            if (counts == null) continue;   

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (allowed == null || allowed.contains(entry.getKey())) {
                    scores.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        }

        return scores.entrySet().stream()
            .map(entry -> new Hit(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingInt(Hit::score).reversed().thenComparing(Hit::file))
            .toList();
    }

    public static List<String> format(List<Hit> hits) {
        if (hits.isEmpty()) return List.of("No results found.");

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            Hit hit = hits.get(i);
            lines.add((i + 1) + ". " + hit.file() + "  (score: " + hit.score() + ")");
        }
        return lines;
    }
}
