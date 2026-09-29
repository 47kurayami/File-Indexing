import java.util.*;

public class CollectionManager {
    // collection name -> set of file names
    private Map<String, Set<String>> collections = new HashMap<>();

    public boolean create(String name) {
        if (collections.containsKey(name)) return false;
        collections.put(name, new HashSet<>());
        return true;
    }
    
public Set<String> getNames() {
    return new TreeSet<>(collections.keySet());
}

    public boolean addFile(String name, String fileName) {
        Set<String> files = collections.get(name);
        if (files == null) return false;
        return files.add(fileName);   // false if already in it
    }

    public boolean exists(String name) {
        return collections.containsKey(name);
    }

    public Set<String> get(String name) {
        return collections.get(name);
    }

    public void showAll() {
        if (collections.isEmpty()) {
            System.out.println("No collections yet.");
            return;
        }
        collections.forEach((name, files) ->
            System.out.println(name + " -> " + files));
    }
}