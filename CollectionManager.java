import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionManager {
    private final Map<String, Set<String>> collections = new TreeMap<>();

    public boolean create(String name) {
        return collections.putIfAbsent(name, new TreeSet<>()) == null;
    }

    public boolean addFile(String name, String fileName) {
        Set<String> files = collections.get(name);
        return files != null && files.add(fileName);
    }

    public boolean exists(String name) {
        return collections.containsKey(name);
    }

    public Set<String> get(String name) {
        return collections.getOrDefault(name, Set.of());
    }

    public Set<String> names() {
        return collections.keySet();
    }

    public void retainAll(Set<String> indexedFiles) {
        collections.values().forEach(files -> files.retainAll(indexedFiles));
    }
}
