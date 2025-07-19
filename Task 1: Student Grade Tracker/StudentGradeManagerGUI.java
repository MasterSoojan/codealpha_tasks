import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StudentGradeManagerGUI
{

    // Data model for a student
    static class Student
    {
        String name;
        double marks;

        Student(String name, double marks)
        {
            this.name = name;
            this.marks = marks;
        }
    }

    // Class-level list to hold student data
    private final ArrayList<Student> students = new ArrayList<>();

    // UI Components
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField marksField;
    private JLabel errorLabel;
    private boolean isUpdatingTable = false; // Flag to prevent listener feedback loops

    public static void main(String[] args)
    {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new StudentGradeManagerGUI().createGUI());
    }

    /**
     * Calculates the letter grade based on the provided marks.
     */
    private static String calculateGrade(double marks)
    {
        if (marks >= 90) return "O";
        if (marks >= 80) return "E";
        if (marks >= 70) return "A";
        if (marks >= 60) return "B";
        if (marks >= 50) return "C";
        if (marks >= 40) return "D";
        return "F";
    }

    private void createGUI()
    {
        // --- UI Styling Constants ---
        Color darkBackground = new Color(30, 30, 30);
        Color componentBg = new Color(50, 50, 50);
        Color greenText = new Color(40, 200, 40);
        Color gridColor = new Color(80, 80, 80);
        Font mainFont = new Font("Consolas", Font.PLAIN, 16);
        Font titleFont = new Font("Arial", Font.BOLD, 24);
        Font labelFont = new Font("Arial", Font.PLAIN, 16);

        // --- Main Frame Setup ---
        JFrame frame = new JFrame("Student Marks Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 600); // Adjusted width
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(darkBackground);

        // --- Main Panel with BorderLayout ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(darkBackground);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Title ---
        JLabel title = new JLabel("Students Grade Manager", SwingConstants.CENTER);
        title.setFont(titleFont);
        title.setForeground(greenText);
        mainPanel.add(title, BorderLayout.NORTH);

        // --- Input Panel (West) ---
        JPanel inputPanel = createInputPanel(labelFont, greenText, mainFont, componentBg);

        // --- Output Table (Center) ---
        String[] columnNames = {"Name", "Marks", "Grade"};
        tableModel = new DefaultTableModel(columnNames, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                // Allow editing for Name and Marks columns only
                return column < 2;
            }
        };

        studentTable = new JTable(tableModel);
        styleTable(studentTable, mainFont, darkBackground, greenText, gridColor, componentBg);

        // --- Add listener to handle table edits ---
        tableModel.addTableModelListener(this::handleTableEdit);


        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(darkBackground);
        scrollPane.setBorder(BorderFactory.createLineBorder(gridColor));


        // --- Control Buttons Panel (South) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(darkBackground);
        JButton reportButton = new JButton("Generate Report");
        JButton clearButton = new JButton("Clear All");

        JButton[] buttons = {reportButton, clearButton};
        for(JButton button : buttons)
        {
            button.setFont(labelFont);
            button.setBackground(componentBg);
            button.setForeground(greenText);
            controlPanel.add(button);
        }

        // --- Add Panels to Main Panel ---
        mainPanel.add(inputPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        reportButton.addActionListener(e -> generateReport());
        clearButton.addActionListener(e -> clearAll());

        // --- Finalize Frame ---
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    private JPanel createInputPanel(Font font, Color color, Font fieldFont, Color fieldBg)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(color), "Add New Student", 0, 0, font, color));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name input
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(font); nameLabel.setForeground(color);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        nameField = new JTextField(15);
        nameField.setFont(fieldFont); nameField.setBackground(fieldBg); nameField.setForeground(color); nameField.setCaretColor(Color.WHITE);
        panel.add(nameField, gbc);

        // Marks input
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel marksLabel = new JLabel("Marks:");
        marksLabel.setFont(font); marksLabel.setForeground(color);
        panel.add(marksLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        marksField = new JTextField(15);
        marksField.setFont(fieldFont); marksField.setBackground(fieldBg); marksField.setForeground(color); marksField.setCaretColor(Color.WHITE);
        panel.add(marksField, gbc);

        // Add Student Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Student");
        addButton.setFont(font); addButton.setBackground(fieldBg); addButton.setForeground(color);
        addButton.addActionListener(e -> addStudent());
        panel.add(addButton, gbc);
        
        // --- Add ActionListeners for Enter key ---
        nameField.addActionListener(e -> marksField.requestFocusInWindow()); // Enter on name field moves to marks
        marksField.addActionListener(e -> addStudent()); // Enter on marks field adds the student

        // Error Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        panel.add(errorLabel, gbc);

        return panel;
    }

    private void styleTable(JTable table, Font font, Color bg, Color fg, Color grid, Color headerBg)
    {
        table.setFont(font);
        table.setBackground(bg);
        table.setForeground(fg);
        table.setGridColor(grid);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(headerBg);
        header.setForeground(fg);
        header.setFont(font.deriveFont(Font.BOLD));
        header.setReorderingAllowed(false);
        
        // --- Set Column Widths and Centering ---
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(250); // Name
        columnModel.getColumn(1).setPreferredWidth(100); // Marks
        columnModel.getColumn(2).setPreferredWidth(100); // Grade

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(1).setCellRenderer(centerRenderer); // Center Marks
        columnModel.getColumn(2).setCellRenderer(centerRenderer); // Center Grade
    }

    private void handleTableEdit(TableModelEvent e)
    {
        if (e.getType() == TableModelEvent.UPDATE && !isUpdatingTable)
        {
            isUpdatingTable = true; // Prevent feedback loop

            int row = e.getFirstRow();
            int col = e.getColumn();
            Object newValue = tableModel.getValueAt(row, col);
            Student student = students.get(row);

            if (col == 0) // Name column updated
            {
                student.name = (String) newValue;
            }
            else if (col == 1) // Marks column updated
            {
                try
                {
                    double newMarks = Double.parseDouble(newValue.toString());
                    if (newMarks < 0 || newMarks > 100)
                    {
                        throw new NumberFormatException("Marks out of range.");
                    }
                    student.marks = newMarks;
                    // Update the grade in the table model
                    tableModel.setValueAt(calculateGrade(newMarks), row, 2);
                }
                catch (NumberFormatException ex)
                {
                    // Revert the invalid edit in the table and show an error
                    JOptionPane.showMessageDialog(null, "Invalid marks. Please enter a number between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    SwingUtilities.invokeLater(() -> tableModel.setValueAt(student.marks, row, col));
                }
            }
            isUpdatingTable = false;
        }
    }

    private void addStudent()
    {
        String name = nameField.getText().trim();
        String marksStr = marksField.getText().trim();

        if (name.isEmpty() || marksStr.isEmpty())
        {
            errorLabel.setText("Name and marks cannot be empty.");
            return;
        }

        try
        {
            double marks = Double.parseDouble(marksStr);
            if (marks < 0 || marks > 100)
            {
                 errorLabel.setText("Marks must be between 0 and 100.");
                 return;
            }
            students.add(new Student(name, marks));
            refreshTable();
            nameField.setText("");
            marksField.setText("");
            nameField.requestFocusInWindow();
            errorLabel.setText(" ");
        }
        catch (NumberFormatException ex)
        {
            errorLabel.setText("Please enter a valid number for marks.");
        }
    }

    private void refreshTable()
    {
        tableModel.setRowCount(0);
        for (Student s : students)
        {
            Object[] rowData = {s.name, s.marks, calculateGrade(s.marks)};
            tableModel.addRow(rowData);
        }
    }

    private void generateReport()
    {
        if (students.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Please add at least one student.", "Report Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double total = 0;
        double highestMark = -1;
        double lowestMark = 101;

        for (Student s : students)
        {
            total += s.marks;
            if (s.marks > highestMark) highestMark = s.marks;
            if (s.marks < lowestMark) lowestMark = s.marks;
        }

        // Find all students who match the highest and lowest marks
        double finalHighest = highestMark;
        String topPerformers = students.stream()
                .filter(s -> s.marks == finalHighest)
                .map(s -> s.name)
                .collect(Collectors.joining(", "));

        double finalLowest = lowestMark;
        String bottomPerformers = students.stream()
                .filter(s -> s.marks == finalLowest)
                .map(s -> s.name)
                .collect(Collectors.joining(", "));

        double average = total / students.size();
        
        StringBuilder report = new StringBuilder();
        report.append("----------- STATISTICS -----------\n\n");
        report.append(String.format("Class Average Marks: %.2f\n\n", average));
        report.append(String.format("Highest Marks: %.2f\n(by %s)\n\n", highestMark, topPerformers));
        report.append(String.format("Lowest Marks:  %.2f\n(by %s)\n", lowestMark, bottomPerformers));

        JTextArea reportArea = new JTextArea(report.toString());
        reportArea.setFont(new Font("Consolas", Font.BOLD, 16));
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(50, 50, 50));
        reportArea.setForeground(new Color(40, 200, 40));
        JOptionPane.showMessageDialog(null, reportArea, "Class Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearAll()
    {
        students.clear();
        refreshTable();
        nameField.setText("");
        marksField.setText("");
        errorLabel.setText(" ");
    }
}
