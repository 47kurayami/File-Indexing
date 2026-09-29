import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        FileIndexer indexer = new FileIndexer();
        indexer.indexFolder(new File("test")); // your test folder path

        indexer.getIndex().forEach((word, fileMap) -> {
            System.out.println(word + " -> " + fileMap);
        });
    }
}