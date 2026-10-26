import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class SearchHistory {
    public record Entry(String query, int resultCount, LocalDateTime time) {
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        @Override
        public String toString() {
            return "[" + time.format(FORMAT) + "] \"" + query + "\" -> " + resultCount + " result(s)";
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void add(String query, int resultCount) {
        entries.add(new Entry(query, resultCount, LocalDateTime.now()));
    }

    
    public List<String> lines() {
        List<String> lines = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            lines.add(entries.get(i).toString());
        }
        return lines;
    }

    public void clear() {
        entries.clear();
    }
}
