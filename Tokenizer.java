//tokenizer, clean the raw text
import java.util.*;

public class Tokenizer {
    public static List<String> tokenize(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String w : words) {
            if (!w.isBlank()) {
                tokens.add(w);
            }
        }
        return tokens;
    }
}
