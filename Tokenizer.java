import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Tokenizer {
    private static final Pattern SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}\\p{M}_]+");

    private Tokenizer() {
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) return tokens;

        for (String word : SEPARATORS.split(text.toLowerCase(Locale.ROOT))) {
            if (!word.isEmpty()) tokens.add(word);
        }
        return tokens;
    }
}
