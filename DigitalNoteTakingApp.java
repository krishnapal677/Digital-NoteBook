package Project;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdvancedDigitalNotebook extends JFrame {

    private JTextArea textArea;
    private JTextField titleField;
    private JComboBox<String> subjectComboBox;
    private JComboBox<String> penColorComboBox;
    private JList<String> notesList;
    private DefaultListModel<String> listModel;
    private Connection conn;
    private Map<String, Color> penColors;
    private JButton colorPreviewButton;

    public AdvancedDigitalNotebook() {
        setTitle("Advanced Digital Notebook");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize database connection
        connectDB();
        createTablesIfNotExists();

        // Initialize pen colors
        initializePenColors();

        // Create GUI components
        initializeGUI();

        // Load initial data
        loadSubjects();
        loadNotes();
    }

    private void initializePenColors() {
        penColors = new LinkedHashMap<>();
        penColors.put("Black", Color.BLACK);
        penColors.put("Blue", Color.BLUE);
        penColors.put("Red", Color.RED);
        penColors.put("Green", new Color(0, 128, 0));
        penColors.put("Purple", new Color(128, 0, 128));
        penColors.put("Orange", Color.ORANGE);
        penColors.put("Gray", Color.GRAY);
        penColors.put("Dark Blue", new Color(0, 0, 139));
    }

    private void initializeGUI() {

            // Main panel with border layout
            setLayout(new BorderLayout(10, 10));

            // Top panel - Title and subject selection
            JPanel topPanel = new JPanel(new GridBagLayout());
            topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Title label and field
            gbc.gridx = 0; gbc.gridy = 0;
            topPanel.add(new JLabel("Note Title:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            titleField = new JTextField();
            topPanel.add(titleField, gbc);

            // Subject label and combo box
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            topPanel.add(new JLabel("Subject:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
            subjectComboBox = new JComboBox<>();
            subjectComboBox.setEditable(true);
            topPanel.add(subjectComboBox, gbc);

            // Pen color label and combo box
            gbc.gridx = 0; gbc.gridy = 2;
            topPanel.add(new JLabel("Pen Color:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            penColorComboBox = new JComboBox<>(penColors.keySet().toArray(new String[0]));
            penColorComboBox.setSelectedItem("Black");
            topPanel.add(penColorComboBox, gbc);

            // Color preview button
            gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
            colorPreviewButton = new JButton();
            colorPreviewButton.setPreferredSize(new Dimension(30, 30));
            topPanel.add(colorPreviewButton, gbc);

            add(topPanel, BorderLayout.NORTH);

            // Center panel - Split pane for notes list and text area
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(250);

            // Left panel - Notes list
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBorder(new TitledBorder("Saved Notes"));

            listModel = new DefaultListModel<>();
            notesList = new JList<>(listModel);
            notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listScrollPane = new JScrollPane(notesList);
            leftPanel.add(listScrollPane, BorderLayout.CENTER);

            // Right panel - Text area
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBorder(new TitledBorder("Note Content"));

            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane textScrollPane = new JScrollPane(textArea);
            rightPanel.add(textScrollPane, BorderLayout.CENTER);

            // ✅ Now it's safe to call updateColorPreview
            updateColorPreview();

            splitPane.setLeftComponent(leftPanel);
            splitPane.setRightComponent(rightPanel);

            add(splitPane, BorderLayout.CENTER);

            // Bottom panel - Buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

            JButton newSubjectButton = new JButton("New Subject");
            JButton newNoteButton = new JButton("New Note");
            JButton saveButton = new JButton("Save Note");
            JButton deleteButton = new JButton("Delete Note");
            JButton searchButton = new JButton("Search");

            bottomPanel.add(newSubjectButton);
            bottomPanel.add(newNoteButton);
            bottomPanel.add(saveButton);
            bottomPanel.add(deleteButton);
            bottomPanel.add(searchButton);

            add(bottomPanel, BorderLayout.SOUTH);

            // Event listeners
            setupEventListeners(newSubjectButton, newNoteButton, saveButton, deleteButton, searchButton);
        }




    private void setupEventListeners(JButton newSubjectButton, JButton newNoteButton,
                                     JButton saveButton, JButton deleteButton, JButton searchButton) {
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedNote = notesList.getSelectedValue();
                if (selectedNote != null) loadNoteContent(selectedNote);
            }
        });

        penColorComboBox.addActionListener(e -> updateColorPreview());

        newSubjectButton.addActionListener(e -> {
            String newSubject = JOptionPane.showInputDialog(this, "Enter new subject name:");
            if (newSubject != null && !newSubject.trim().isEmpty()) {
                addSubject(newSubject.trim());
            }
        });

        newNoteButton.addActionListener(e -> {
            titleField.setText("");
            textArea.setText("");
            textArea.setForeground(penColors.get(penColorComboBox.getSelectedItem()));
            notesList.clearSelection();
        });

        saveButton.addActionListener(e -> saveNote());
        deleteButton.addActionListener(e -> deleteNote());
        searchButton.addActionListener(e -> searchNotes());
    }

    private void updateColorPreview() {
        String selectedColor = (String) penColorComboBox.getSelectedItem();
        Color color = penColors.get(selectedColor);
        colorPreviewButton.setBackground(color);
        textArea.setForeground(color);
    }

    // ✅ MySQL Connection
    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/digital_notebook?useSSL=false&serverTimezone=UTC",
                    "root",       // your MySQL username
                    "Krishna@0939"    // your MySQL password
            );
            System.out.println("✅ Connected to MySQL Database!");
        } catch (Exception e) {
            showError("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ MySQL Tables
    private void createTablesIfNotExists() {
        try (Statement stmt = conn.createStatement()) {
            String subjectsSql = "CREATE TABLE IF NOT EXISTS subjects (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) NOT NULL UNIQUE," +
                    "created_at DATETIME NOT NULL" +
                    ")";
            stmt.execute(subjectsSql);

            String notesSql = "CREATE TABLE IF NOT EXISTS notes (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "title VARCHAR(255) NOT NULL," +
                    "content TEXT NOT NULL," +
                    "subject_id INT," +
                    "color VARCHAR(20) NOT NULL," +
                    "created_at DATETIME NOT NULL," +
                    "updated_at DATETIME NOT NULL," +
                    "FOREIGN KEY (subject_id) REFERENCES subjects(id)" +
                    ")";
            stmt.execute(notesSql);

            System.out.println("✅ Tables created (if not already).");
        } catch (SQLException e) {
            showError("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSubjects() {
        subjectComboBox.removeAllItems();
        String sql = "SELECT name FROM subjects ORDER BY name";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) subjectComboBox.addItem(rs.getString("name"));
        } catch (SQLException ignored) {}
    }

    private void loadNotes() {
        listModel.clear();
        String sql = "SELECT n.title, s.name as subject FROM notes n " +
                "LEFT JOIN subjects s ON n.subject_id = s.id " +
                "ORDER BY n.updated_at DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String title = rs.getString("title");
                String subject = rs.getString("subject");
                listModel.addElement(subject != null ? subject + " - " + title : title);
            }
        } catch (SQLException ignored) {}
    }

    private void loadNoteContent(String noteInfo) {
        String[] parts = noteInfo.split(" - ", 2);
        String title = parts.length > 1 ? parts[1] : parts[0];
        String sql = "SELECT n.content, n.color, s.name as subject FROM notes n " +
                "LEFT JOIN subjects s ON n.subject_id = s.id WHERE n.title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                titleField.setText(title);
                textArea.setText(rs.getString("content"));
                Color color = parseColor(rs.getString("color"));
                textArea.setForeground(color);
                String subject = rs.getString("subject");
                if (subject != null) subjectComboBox.setSelectedItem(subject);
                for (String key : penColors.keySet()) {
                    if (penColors.get(key).equals(color)) {
                        penColorComboBox.setSelectedItem(key);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Error loading note: " + e.getMessage());
        }
    }

    private void addSubject(String subjectName) {
        String sql = "INSERT IGNORE INTO subjects (name, created_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(1, subjectName);
            pstmt.setString(2, now);
            pstmt.executeUpdate();
            loadSubjects();
            subjectComboBox.setSelectedItem(subjectName);
        } catch (SQLException e) {
            showError("Error adding subject: " + e.getMessage());
        }
    }

    private void saveNote() {
        String title = titleField.getText().trim();
        String content = textArea.getText();
        String subjectName = (String) subjectComboBox.getSelectedItem();
        String colorName = (String) penColorComboBox.getSelectedItem();
        Color color = penColors.get(colorName);
        String colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        if (title.isEmpty()) {
            showError("Title cannot be empty.");
            return;
        }

        try {
            Integer subjectId = null;
            if (subjectName != null && !subjectName.trim().isEmpty()) {
                String subjectSql = "INSERT IGNORE INTO subjects (name, created_at) VALUES (?, ?)";
                try (PreparedStatement subjectStmt = conn.prepareStatement(subjectSql)) {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    subjectStmt.setString(1, subjectName);
                    subjectStmt.setString(2, now);
                    subjectStmt.executeUpdate();
                }
                String getIdSql = "SELECT id FROM subjects WHERE name = ?";
                try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql)) {
                    getIdStmt.setString(1, subjectName);
                    ResultSet rs = getIdStmt.executeQuery();
                    if (rs.next()) subjectId = rs.getInt("id");
                }
            }

            String checkSql = "SELECT id FROM notes WHERE title = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (rs.next()) {
                String updateSql = "UPDATE notes SET content = ?, subject_id = ?, color = ?, updated_at = ? WHERE title = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, content);
                    updateStmt.setObject(2, subjectId);
                    updateStmt.setString(3, colorHex);
                    updateStmt.setString(4, now);
                    updateStmt.setString(5, title);
                    updateStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Note updated successfully!");
            } else {
                String insertSql = "INSERT INTO notes (title, content, subject_id, color, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, title);
                    insertStmt.setString(2, content);
                    insertStmt.setObject(3, subjectId);
                    insertStmt.setString(4, colorHex);
                    insertStmt.setString(5, now);
                    insertStmt.setString(6, now);
                    insertStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Note saved successfully!");
            }
            loadNotes();
        } catch (SQLException e) {
            showError("Error saving note: " + e.getMessage());
        }
    }

    private void deleteNote() {
        String selectedNote = notesList.getSelectedValue();
        if (selectedNote == null) {
            showError("Select a note to delete.");
            return;
        }
        String[] parts = selectedNote.split(" - ", 2);
        String title = parts.length > 1 ? parts[1] : parts[0];
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + title + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM notes WHERE title = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Note deleted!");
                titleField.setText("");
                textArea.setText("");
                loadNotes();
            } catch (SQLException e) {
                showError("Error deleting note: " + e.getMessage());
            }
        }
    }

    private void searchNotes() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter search term:");
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            listModel.clear();
            String sql = "SELECT n.title, s.name as subject FROM notes n " +
                    "LEFT JOIN subjects s ON n.subject_id = s.id " +
                    "WHERE n.title LIKE ? OR n.content LIKE ? OR s.name LIKE ? " +
                    "ORDER BY n.updated_at DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String likeTerm = "%" + searchTerm + "%";
                pstmt.setString(1, likeTerm);
                pstmt.setString(2, likeTerm);
                pstmt.setString(3, likeTerm);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String title = rs.getString("title");
                    String subject = rs.getString("subject");
                    listModel.addElement(subject != null ? subject + " - " + title : title);
                }
            } catch (SQLException e) {
                showError("Error searching notes: " + e.getMessage());
            }
        }
    }

    private Color parseColor(String colorHex) {
        try { return Color.decode(colorHex); }
        catch (Exception e) { return Color.BLACK; }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new AdvancedDigitalNotebook().setVisible(true);
        });
    }
}
