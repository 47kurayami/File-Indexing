import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        FileIndexer indexer = new FileIndexer();
        CollectionManager collections = new CollectionManager();

        try {
            reindex(indexer, collections, new File(args.length > 0 ? args[0] : "."));
        } catch (IOException e) {
            System.out.println("Could not index: " + e.getMessage());
        }

        SearchEngine engine = new SearchEngine(indexer.index());
        SearchHistory history = new SearchHistory();
        Scanner sc = new Scanner(System.in);
        String scope = null;   

        System.out.println("Commands: index <folder> | files | create <name> | add <collection> <file>");
        System.out.println("          collections | use <name|all> | history | clear | search <query> | exit");

        while (true) {
            System.out.print("\n[" + (scope == null ? "all files" : scope) + "] Search or command: ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 2);
            String cmd = parts[0].toLowerCase(Locale.ROOT);
            String rest = parts.length > 1 ? parts[1] : "";

            switch (cmd) {
                case "exit" -> {
                    return;
                }
                case "search" -> {
                    if (rest.isEmpty()) System.out.println("Usage: search <query>");
                    else search(engine, history, collections, scope, rest);
                }
                case "index" -> {
                    if (rest.isEmpty()) System.out.println("Usage: index <folder>");
                    else {
                        try {
                            reindex(indexer, collections, new File(rest));
                        } catch (IOException e) {
                            System.out.println("Could not index: " + e.getMessage());
                        }
                    }
                }
                case "files" -> {
                    if (indexer.files().isEmpty()) System.out.println("No files indexed.");
                    else indexer.files().forEach(System.out::println);
                }
                case "create" -> {
                    if (rest.isEmpty()) System.out.println("Usage: create <name>");
                    else System.out.println(collections.create(rest)
                            ? "Created collection: " + rest
                            : "Already exists.");
                }
                case "add" -> addFile(collections, indexer, rest);
                case "collections" -> showCollections(collections);
                case "use" -> {
                    if (rest.equalsIgnoreCase("all")) scope = null;
                    else if (collections.exists(rest)) scope = rest;
                    else System.out.println("No such collection.");
                }
                case "history" -> printLines(history.lines(), "No search history yet.");
                case "clear" -> {
                    history.clear();
                    System.out.println("History cleared.");
                }
                default -> search(engine, history, collections, scope, input);
            }
        }
    }

    private static void reindex(FileIndexer indexer, CollectionManager collections, File folder)
            throws IOException {
        indexer.clear();
        int count = indexer.indexFolder(folder);
        collections.retainAll(indexer.files());
        System.out.println(count + " file(s) indexed from " + folder.getPath());
    }

    private static void search(SearchEngine engine, SearchHistory history,
                               CollectionManager collections, String scope, String query) {
        Set<String> allowed = scope == null ? null : collections.get(scope);
        List<SearchEngine.Hit> hits = engine.search(query, allowed);
        history.add(query, hits.size());
        SearchEngine.format(hits).forEach(System.out::println);
    }

    private static void addFile(CollectionManager collections, FileIndexer indexer, String rest) {
        String[] args = rest.split("\\s+", 2);
        if (args.length < 2) {
            System.out.println("Usage: add <collection> <file>");
        } else if (!collections.exists(args[0])) {
            System.out.println("No such collection.");
        } else if (!indexer.files().contains(args[1])) {
            System.out.println("File not indexed. Type 'files' to see options.");
        } else {
            System.out.println(collections.addFile(args[0], args[1]) ? "Added." : "Already in collection.");
        }
    }

    private static void showCollections(CollectionManager collections) {
        if (collections.names().isEmpty()) {
            System.out.println("No collections yet.");
            return;
        }
        for (String name : collections.names()) {
            System.out.println(name + " -> " + collections.get(name));
        }
    }

    private static void printLines(List<String> lines, String emptyMessage) {
        if (lines.isEmpty()) System.out.println(emptyMessage);
        else lines.forEach(System.out::println);
    }
}
