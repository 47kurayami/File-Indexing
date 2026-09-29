import java.io.*;        // File, IOException
import java.nio.file.*;  // Files (for Files.readString)
import java.util.*;      // Map, HashMap, List, Set, HashSet

public class FileIndexer {
    // word -> (filename -> count)
    private Map<String, Map<String, Integer>> index = new HashMap<>();

    // names of every file that has been indexed
    private Set<String> indexedFiles = new HashSet<>();

    public void clear() {
    index.clear();
    indexedFiles.clear();
}

    public void indexFolder(File folder) throws IOException {
        System.out.println("Looking in: " + folder.getAbsolutePath());
        System.out.println("Exists? " + folder.exists() + ", is directory? " + folder.isDirectory());

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) {
            System.out.println("Could not read folder.");
            return;
        }
        System.out.println("Found " + files.length + " .txt file(s)");

        for (File file : files) {
            indexFile(file);
        }
    }

    private void indexFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        List<String> words = Tokenizer.tokenize(content);
        String fileName = file.getName();

        indexedFiles.add(fileName);   // NEW

        for (String word : words) {
            index.putIfAbsent(word, new HashMap<>());
            Map<String, Integer> fileCounts = index.get(word);
            fileCounts.put(fileName, fileCounts.getOrDefault(fileName, 0) + 1);
        }
    }

    public Map<String, Map<String, Integer>> getIndex() {
        return index;
    }

    // NEW
    public Set<String> getIndexedFiles() {
        return indexedFiles;
    }
}