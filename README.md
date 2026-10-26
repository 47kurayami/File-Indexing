# File Indexing

A small Java project for indexing a folder of documents and searching them by keyword.

It walks the folder (subfolders included), pulls the text out of each supported file,
and builds an inverted index: word -> {file -> count}. A search looks up each query
word and adds up the counts per file, so files that use those words more often end up
higher. Files are stored by their path relative to the indexed folder, so two
`notes.txt` in different folders don't get mixed together.

Supported: `.txt`, `.md`, `.csv`, `.json`, `.html`, `.docx`, `.pdf`, plus the usual
code/config extensions (see `PLAIN` in `DocumentReader`). PDFs need PDFBox 3.x on the
classpath at runtime. Everything else works with plain `javac`.

## Running

    javac *.java

    java MainApp          # GUI
    java Main [folder]    # command line, defaults to the current folder

## CLI

    index <folder>          find/re-index files
    files                   show what got indexed
    create <name>           make a collection
    add <collection> <file> put an indexed file in it
    collections             list collections
    use <name|all>          limit searches to one collection
    history / clear         search history
    search <query>          use this if the query is also a command name
    exit

Anything else you type is treated as a search.

## Notes

- Scores are raw word counts, so long files have an advantage. Fine for a small
  local index, not a real ranking algorithm.
- Searching is OR-based: a file matches if it has any of the words.
- Collections and search history live in memory and disappear when you exit.
- The index is rebuilt from scratch each time you pick a folder.
