import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SearchRecord {
    private String query;
    private int resultCount;
    private LocalDateTime time;

    public SearchRecord(String query, int resultCount) {
        this.query = query;
        this.resultCount = resultCount;
        this.time = LocalDateTime.now();
    }

    public String getQuery() { return query; }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "[" + time.format(f) + "] \"" + query + "\" -> " + resultCount + " result(s)";
    }
}