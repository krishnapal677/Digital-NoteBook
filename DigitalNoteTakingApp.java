import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DigitalNoteTakingApp extends JFrame {
    private JTextArea noteArea;
    private JTextField searchField;
    private DefaultListModel<String> noteListModel;
    private JList<String> noteList;
    private List<Note> notes;

    public DigitalNoteTakingApp() {
        notes = new ArrayList<>();
        setTitle("Digital Note-Taking Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Note List
        noteListModel = new DefaultListModel<>();
        noteList = new JList<>(noteListModel);
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                loadSelectedNote();
            }
        });
        JScrollPane noteListScrollPane = new JScrollPane(noteList);
        add(noteListScrollPane, BorderLayout.WEST);

        // Note Area
        noteArea = new JTextArea();
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteAreaScrollPane = new JScrollPane(noteArea);
        add(noteAreaScrollPane, BorderLayout.CENTER);

        // Search Field
        searchField = new JTextField();
        searchField.addActionListener(e -> searchNotes());
        add(searchField, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save Note");
        saveButton.addActionListener(e -> saveNote());
        buttonPanel.add(saveButton);

        JButton deleteButton = new JButton("Delete Note");
        deleteButton.addActionListener(e -> deleteNote());
        buttonPanel.add(deleteButton);

        JButton bgColorButton = new JButton("Change Background Color");
        bgColorButton.addActionListener(e -> changeBackgroundColor());
        buttonPanel.add(bgColorButton);

        JButton textColorButton = new JButton("Change Text Color");
        textColorButton.addActionListener(e -> changeTextColor());
        buttonPanel.add(textColorButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveNote() {
        String noteContent = noteArea.getText();
        String noteTitle = JOptionPane.showInputDialog(this, "Enter note title:");
        if (noteTitle != null && !noteTitle.trim().isEmpty()) {
            Note note = new Note(noteTitle, noteContent);
            notes.add(note);
            noteListModel.addElement(noteTitle);
            noteArea.setText("");
        }
    }

    private void loadSelectedNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            Note selectedNote = notes.get(selectedIndex);
            noteArea.setText(selectedNote.getContent());
        }
    }

    private void deleteNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            notes.remove(selectedIndex);
            noteListModel.remove(selectedIndex);
            noteArea.setText("");
        }
    }

    private void searchNotes() {
        String searchText = searchField.getText().toLowerCase();
        noteListModel.clear();
        for (Note note : notes) {
            if (note.getTitle().toLowerCase().contains(searchText)) {
                noteListModel.addElement(note.getTitle());
            }
        }
    }

    private void changeBackgroundColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Background Color", noteArea.getBackground());
        if (newColor != null) {
            noteArea.setBackground(newColor);
        }
    }

    private void changeTextColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Text Color", noteArea.getForeground());
        if (newColor != null) {
            noteArea.setForeground(newColor);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DigitalNoteTakingApp app = new DigitalNoteTakingApp();
            app.setVisible(true);
        });
    }

    private static class Note {
        private String title;
        private String content;

        public Note(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}

