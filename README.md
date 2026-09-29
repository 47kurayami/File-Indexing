
# Local Document Indexing & Search Engine

A Java app that indexes local documents and searches them by keyword.

## Features
- Index a folder of .txt, .md, .csv, .json, .html, .docx (and .pdf with PDFBox)
- Keyword frequency ranking across multiple documents
- Search history with timestamps
- Collections: named groups of files to search within

## Run
    javac *.java
    java MainApp        # GUI
    java Main           # command line

## PDF support (optional)
Download pdfbox-app-3.0.x.jar into this folder, then:
    java -cp ".:pdfbox-app-3.0.x.jar" MainApp

## How it works
Each file is read, split into words, and stored in an inverted index
(HashMap: word -> {file -> count}). A search looks up each query word
and adds up the counts per file to rank results.