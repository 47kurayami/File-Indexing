import java.io.*;
import java.util.*;

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

        File[] files = folder.listFiles(DocumentReader::isSupported);
        if (files == null) {
            System.out.println("Could not read folder.");
            return;
        }
        System.out.println("Found " + files.length + " supported file(s)");

        for (File file : files) {
            try {
                indexFile(file);
            } catch (IOException e) {
                System.out.println("Skipped " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private void indexFile(File file) throws IOException {
        String content = DocumentReader.read(file);
        List<String> words = Tokenizer.tokenize(content);
        String fileName = file.getName();

        indexedFiles.add(fileName);

        for (String word : words) {
            index.putIfAbsent(word, new HashMap<>());
            Map<String, Integer> fileCounts = index.get(word);
            fileCounts.put(fileName, fileCounts.getOrDefault(fileName, 0) + 1);
        }
    }

    public Map<String, Map<String, Integer>> getIndex() {
        return index;
    }

    public Set<String> getIndexedFiles() {
        return indexedFiles;
    }
}