import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class FileIndexer {
    private final Map<String, Map<String, Integer>> index = new HashMap<>();
    private final Set<String> indexedFiles = new TreeSet<>();

    public void clear() {
        index.clear();
        indexedFiles.clear();
    }

    public int indexFolder(File folder) throws IOException {
        Path root = folder.toPath();
        if (!Files.isDirectory(root)) throw new IOException("Folder not found: " + root);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .map(Path::toFile)
                 .filter(DocumentReader::isSupported)
                 .forEach(file -> indexSafely(root, file));
        } catch (UncheckedIOException e) {
            throw new IOException("Cannot read folder: " + e.getCause().getMessage(), e.getCause());
        }
        return indexedFiles.size();
    }

    private void indexSafely(Path root, File file) {
        try {
            indexFile(root.relativize(file.toPath()).toString(), file);
        } catch (IOException e) {
            System.err.println("Skipped " + file.getName() + ": " + e.getMessage());
        }
    }

    private void indexFile(String name, File file) throws IOException {
        indexedFiles.add(name);
        for (String word : Tokenizer.tokenize(DocumentReader.read(file))) {
            index.computeIfAbsent(word, w -> new HashMap<>())
                 .merge(name, 1, Integer::sum);
        }
    }

    public Map<String, Map<String, Integer>> index() {
        return index;
    }

    public Set<String> files() {
        return indexedFiles;
    }
}
