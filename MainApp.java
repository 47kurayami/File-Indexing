import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainApp extends JFrame {
    private FileIndexer indexer = new FileIndexer();
    private SearchEngine engine = new SearchEngine(indexer.getIndex());
    private SearchHistory history = new SearchHistory();
    private CollectionManager collections = new CollectionManager();

    private JLabel folderLabel = new JLabel("No folder indexed yet");
    private JTextField searchField = new JTextField(22);
    private JComboBox<String> scopeBox = new JComboBox<>();
    private JTextArea resultsArea = new JTextArea();
    private JTextArea historyArea = new JTextArea();
    private JTextArea collectionsArea = new JTextArea();
    private DefaultListModel<String> fileModel = new DefaultListModel<>();
    private JList<String> fileList = new JList<>(fileModel);
    private JComboBox<String> collectionBox = new JComboBox<>();

    public MainApp() {
        setTitle("Document Search Engine");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton folderBtn = new JButton("Choose Folder to Index");
        folderBtn.addActionListener(e -> chooseFolder());
        top.add(folderBtn);
        top.add(folderLabel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Search", buildSearchTab());
        tabs.addTab("History", buildHistoryTab());
        tabs.addTab("Collections", buildCollectionsTab());

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refreshCollectionUI();
        refreshHistory();
        setVisible(true);
    }

    // ---------- tabs ----------

    private JPanel buildSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());   // Enter key also searches

        bar.add(searchField);
        bar.add(new JLabel("in:"));
        bar.add(scopeBox);
        bar.add(searchBtn);

        resultsArea.setEditable(false);
        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        historyArea.setEditable(false);
        JButton clearBtn = new JButton("Clear History");
        clearBtn.addActionListener(e -> {
            history.clear();
            refreshHistory();
        });
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        panel.add(clearBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCollectionsTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel createBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(15);
        JButton createBtn = new JButton("Create Collection");
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;
            if (!collections.create(name)) {
                JOptionPane.showMessageDialog(this, "Collection already exists.");
            }
            nameField.setText("");
            refreshCollectionUI();
        });
        createBar.add(new JLabel("Name:"));
        createBar.add(nameField);
        createBar.add(createBtn);

        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setPreferredSize(new Dimension(220, 0));
        fileScroll.setBorder(BorderFactory.createTitledBorder("Indexed files"));

        collectionsArea.setEditable(false);
        JScrollPane collScroll = new JScrollPane(collectionsArea);
        collScroll.setBorder(BorderFactory.createTitledBorder("Collections"));

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add selected files to");
        addBtn.addActionListener(e -> addSelectedToCollection());
        addBar.add(addBtn);
        addBar.add(collectionBox);

        panel.add(createBar, BorderLayout.NORTH);
        panel.add(fileScroll, BorderLayout.WEST);
        panel.add(collScroll, BorderLayout.CENTER);
        panel.add(addBar, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- actions ----------

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select folder to index");

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File folder = chooser.getSelectedFile();
        try {
            indexer.clear();
            indexer.indexFolder(folder);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading files: " + ex.getMessage());
            return;
        }

        int count = indexer.getIndexedFiles().size();
        folderLabel.setText(folder.getName() + " (" + count + " file(s) indexed)");

        fileModel.clear();
        for (String f : new TreeSet<>(indexer.getIndexedFiles())) {
            fileModel.addElement(f);
        }
        resultsArea.setText("");
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        if (indexer.getIndexedFiles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Choose a folder to index first.");
            return;
        }

        Set<String> allowed = null;   // null = search all files
        if (scopeBox.getSelectedIndex() > 0) {
            allowed = collections.get((String) scopeBox.getSelectedItem());
        }

        List<Map.Entry<String, Integer>> results = engine.search(query, allowed);
        history.add(query, results.size());
        refreshHistory();

        StringBuilder sb = new StringBuilder();
        if (results.isEmpty()) {
            sb.append("No results found.");
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> r : results) {
                sb.append(rank++).append(". ").append(r.getKey())
                  .append("  (score: ").append(r.getValue()).append(")\n");
            }
        }
        resultsArea.setText(sb.toString());
    }

    private void addSelectedToCollection() {
        String name = (String) collectionBox.getSelectedItem();
        if (name == null) {
            JOptionPane.showMessageDialog(this, "Create a collection first.");
            return;
        }
        List<String> selected = fileList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select one or more files from the list.");
            return;
        }
        for (String f : selected) {
            collections.addFile(name, f);
        }
        refreshCollectionUI();
    }

    // ---------- refresh helpers ----------

    private void refreshHistory() {
        List<String> lines = history.getLines();
        historyArea.setText(lines.isEmpty() ? "No search history yet." : String.join("\n", lines));
    }

    private void refreshCollectionUI() {
        Object prevScope = scopeBox.getSelectedItem();
        Object prevColl = collectionBox.getSelectedItem();

        scopeBox.removeAllItems();
        collectionBox.removeAllItems();
        scopeBox.addItem("All files");
        for (String n : collections.getNames()) {
            scopeBox.addItem(n);
            collectionBox.addItem(n);
        }
        if (prevScope != null) scopeBox.setSelectedItem(prevScope);
        if (prevColl != null) collectionBox.setSelectedItem(prevColl);

        StringBuilder sb = new StringBuilder();
        for (String n : collections.getNames()) {
            sb.append(n).append(" -> ").append(new TreeSet<>(collections.get(n))).append("\n");
        }
        collectionsArea.setText(sb.length() == 0 ? "No collections yet." : sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}