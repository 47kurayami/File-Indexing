import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MainApp extends JFrame {
    private final FileIndexer indexer = new FileIndexer();
    private final SearchEngine engine = new SearchEngine(indexer.index());
    private final SearchHistory history = new SearchHistory();
    private final CollectionManager collections = new CollectionManager();

    private final JLabel folderLabel = new JLabel("No folder indexed yet");
    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> scopeBox = new JComboBox<>();
    private final JTextArea resultsArea = new JTextArea();
    private final JTextArea historyArea = new JTextArea();
    private final JTextArea collectionsArea = new JTextArea();
    private final DefaultListModel<String> fileModel = new DefaultListModel<>();
    private final JList<String> fileList = new JList<>(fileModel);
    private final JComboBox<String> collectionBox = new JComboBox<>();

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

    

    private JPanel buildSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());    

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
        JScrollPane collectionScroll = new JScrollPane(collectionsArea);
        collectionScroll.setBorder(BorderFactory.createTitledBorder("Collections"));

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add selected files to");
        addBtn.addActionListener(e -> addSelectedToCollection());
        addBar.add(addBtn);
        addBar.add(collectionBox);

        panel.add(createBar, BorderLayout.NORTH);
        panel.add(fileScroll, BorderLayout.WEST);
        panel.add(collectionScroll, BorderLayout.CENTER);
        panel.add(addBar, BorderLayout.SOUTH);
        return panel;
    }


    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select folder to index");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File folder = chooser.getSelectedFile();
        indexer.clear();
        try {
            int count = indexer.indexFolder(folder);
            folderLabel.setText(folder.getName() + " (" + count + " file(s) indexed)");
        } catch (IOException ex) {
            folderLabel.setText("No folder indexed yet");
            JOptionPane.showMessageDialog(this, "Could not index folder: " + ex.getMessage());
        }

        collections.retainAll(indexer.files());
        refreshFileList();
        refreshCollectionUI();
        resultsArea.setText("");
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        if (indexer.files().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Choose a folder to index first.");
            return;
        }

        Set<String> allowed = scopeBox.getSelectedIndex() > 0
                ? collections.get((String) scopeBox.getSelectedItem())
                : null;   

        List<SearchEngine.Hit> hits = engine.search(query, allowed);
        history.add(query, hits.size());
        refreshHistory();
        resultsArea.setText(String.join("\n", SearchEngine.format(hits)));
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

        for (String file : selected) {
            collections.addFile(name, file);
        }
        refreshCollectionUI();
    }


    private void refreshFileList() {
        fileModel.clear();
        indexer.files().forEach(fileModel::addElement);
    }

    private void refreshHistory() {
        List<String> lines = history.lines();
        historyArea.setText(lines.isEmpty() ? "No search history yet." : String.join("\n", lines));
    }

    private void refreshCollectionUI() {
        Object previousScope = scopeBox.getSelectedItem();
        Object previousCollection = collectionBox.getSelectedItem();

        scopeBox.removeAllItems();
        collectionBox.removeAllItems();
        scopeBox.addItem("All files");
        for (String name : collections.names()) {
            scopeBox.addItem(name);
            collectionBox.addItem(name);
        }
        if (previousScope != null) scopeBox.setSelectedItem(previousScope);
        if (previousCollection != null) collectionBox.setSelectedItem(previousCollection);

        if (collections.names().isEmpty()) {
            collectionsArea.setText("No collections yet.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (String name : collections.names()) {
            text.append(name).append(" -> ").append(collections.get(name)).append('\n');
        }
        collectionsArea.setText(text.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}
