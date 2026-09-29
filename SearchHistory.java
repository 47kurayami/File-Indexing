import java.util.*;

public class SearchHistory {
    private List<SearchRecord> records = new ArrayList<>();


public List<String> getLines() {
    List<String> lines = new ArrayList<>();
    for (int i = records.size() - 1; i >= 0; i--) {   // newest first
        lines.add(records.get(i).toString());
    }
    return lines;
}


    public void add(String query, int resultCount) {
        records.add(new SearchRecord(query, resultCount));
    }

    public void show() {
        if (records.isEmpty()) {
            System.out.println("No search history yet.");
            return;
        }
        // most recent first
        for (int i = records.size() - 1; i >= 0; i--) {
            System.out.println(records.get(i));
        }
    }

    public void clear() {
        records.clear();
    }
}