import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        FileIndexer indexer = new FileIndexer();
        indexer.indexFolder(new File("test"));

        SearchEngine engine = new SearchEngine(indexer.getIndex());
        SearchHistory history = new SearchHistory();
        CollectionManager collections = new CollectionManager();
        Scanner sc = new Scanner(System.in);

        String activeCollection = null;   // null = search all files

        System.out.println("Commands: files | create <name> | add <collection> <file> | collections | use <name|all> | history | clear | exit");

        while (true) {
            String scope = (activeCollection == null) ? "all files" : activeCollection;
            System.out.print("\n[" + scope + "] Search or command: ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 3);
            String cmd = parts[0].toLowerCase();

            if (cmd.equals("exit")) break;

            else if (cmd.equals("history")) history.show();

            else if (cmd.equals("clear")) { history.clear(); System.out.println("History cleared."); }

            else if (cmd.equals("files")) System.out.println(indexer.getIndexedFiles());

            else if (cmd.equals("collections")) collections.showAll();

            else if (cmd.equals("create") && parts.length >= 2) {
                System.out.println(collections.create(parts[1])
                    ? "Created collection: " + parts[1] : "Already exists.");
            }

            else if (cmd.equals("add") && parts.length == 3) {
                if (!collections.exists(parts[1])) System.out.println("No such collection.");
                else if (!indexer.getIndexedFiles().contains(parts[2])) System.out.println("File not indexed. Type 'files' to see options.");
                else System.out.println(collections.addFile(parts[1], parts[2])
                    ? "Added." : "Already in collection.");
            }

            else if (cmd.equals("use") && parts.length >= 2) {
                if (parts[1].equalsIgnoreCase("all")) activeCollection = null;
                else if (collections.exists(parts[1])) activeCollection = parts[1];
                else System.out.println("No such collection.");
            }

            else {
                // anything else is a search query
                Set<String> allowed = (activeCollection == null) ? null : collections.get(activeCollection);
                List<Map.Entry<String, Integer>> results = engine.search(input, allowed);
                history.add(input, results.size());

                if (results.isEmpty()) System.out.println("No results found.");
                else {
                    int rank = 1;
                    for (Map.Entry<String, Integer> r : results)
                        System.out.println(rank++ + ". " + r.getKey() + " (score: " + r.getValue() + ")");
                }
            }
        }
    }
}