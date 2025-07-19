import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.BorderFactory; // Fixed: Added BorderFactory import
import javax.swing.border.Border;   // Fixed: Added Border import
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component; // Fixed: Added Component import
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// --- Data Models ---

class Room
{
    int roomNumber;
    String category;
    boolean isBooked;

    Room(int roomNumber, String category)
    {
        this.roomNumber = roomNumber;
        this.category = category;
        this.isBooked = false;
    }
}

class Reservation
{
    String name;
    int roomNumber;
    String category;
    String paymentStatus;

    Reservation(String name, int roomNumber, String category, String paymentStatus)
    {
        this.name = name;
        this.roomNumber = roomNumber;
        this.category = category;
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString()
    {
        return name + "," + roomNumber + "," + category + "," + paymentStatus;
    }

    public static Reservation fromString(String data)
    {
        String[] parts = data.split(",");
        return new Reservation(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]);
    }
}

// --- Logic Layer ---

class Hotel
{
    List<Room> rooms = new ArrayList<>();
    List<Reservation> reservations = new ArrayList<>();
    final String FILE_NAME = "reservations.txt";

    Hotel()
    {
        initRooms();
        loadReservationsFromFile();
    }

    void initRooms()
    {
        for (int i = 101; i <= 110; i++) rooms.add(new Room(i, "Standard"));
        for (int i = 201; i <= 205; i++) rooms.add(new Room(i, "Deluxe"));
        for (int i = 301; i <= 303; i++) rooms.add(new Room(i, "Suite"));
    }

    void loadReservationsFromFile()
    {
        File file = new File(FILE_NAME);
        if (!file.exists())
        {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                Reservation res = Reservation.fromString(line);
                reservations.add(res);
                getRoomByNumber(res.roomNumber).ifPresent(room -> room.isBooked = true);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void saveReservationsToFile()
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME)))
        {
            for (Reservation r : reservations)
            {
                bw.write(r.toString());
                bw.newLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Optional<Room> getRoomByNumber(int roomNumber)
    {
        return rooms.stream().filter(r -> r.roomNumber == roomNumber).findFirst();
    }

    public Optional<Reservation> getReservationByRoomNumber(int roomNumber)
    {
        return reservations.stream().filter(r -> r.roomNumber == roomNumber).findFirst();
    }

    String makeReservation(String name, int roomNumber)
    {
        Optional<Room> roomOpt = getRoomByNumber(roomNumber);
        if (roomOpt.isPresent())
        {
            Room room = roomOpt.get();
            if (room.isBooked)
            {
                return "Error: Room " + roomNumber + " is already booked!";
            }
            room.isBooked = true;
            reservations.add(new Reservation(name, room.roomNumber, room.category, "Paid"));
            saveReservationsToFile();
            return "Booking successful! Room " + room.roomNumber + " reserved for " + name + ".";
        }
        return "Error: Room not found.";
    }

    String cancelReservation(int roomNumber)
    {
        Optional<Room> roomOpt = getRoomByNumber(roomNumber);
        if (roomOpt.isPresent() && roomOpt.get().isBooked)
        {
            Room room = roomOpt.get();
            room.isBooked = false;
            reservations.removeIf(res -> res.roomNumber == roomNumber);
            saveReservationsToFile();
            return "Reservation for Room " + roomNumber + " has been cancelled.";
        }
        return "Error: No reservation found for Room " + roomNumber + ".";
    }
}

/**
 * A custom JPanel to visually represent a single hotel room.
 */
class RoomPanel extends JPanel
{
    private Room room;
    public final Border defaultBorder = BorderFactory.createLineBorder(new Color(60, 60, 60));
    public final Border selectedBorder = BorderFactory.createLineBorder(new Color(40, 200, 40), 3);

    RoomPanel(Room room)
    {
        this.room = room;
        setLayout(new BorderLayout());
        setBorder(defaultBorder);

        JLabel roomNumberLabel = new JLabel(String.valueOf(room.roomNumber), SwingConstants.CENTER);
        roomNumberLabel.setFont(new Font("Arial", Font.BOLD, 28));
        roomNumberLabel.setForeground(Color.WHITE);

        JLabel categoryLabel = new JLabel(room.category, SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        categoryLabel.setForeground(Color.LIGHT_GRAY);
        categoryLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        add(roomNumberLabel, BorderLayout.CENTER);
        add(categoryLabel, BorderLayout.SOUTH);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        updateStatus();
    }

    public Room getRoom()
    {
        return room;
    }

    public void updateStatus()
    {
        setBackground(room.isBooked ? new Color(100, 30, 30) : new Color(34, 80, 34));
    }
}

public class HotelReservationGUI
{
    // --- UI Components & State ---
    private Hotel hotel;
    private JPanel roomGridPanel;
    private JPanel actionPanelContainer;
    private JLabel statusLabel;
    private RoomPanel selectedRoomPanel = null;

    // --- Styling Constants ---
    private final Color DARK_BACKGROUND = new Color(30, 30, 30);
    private final Color COMPONENT_BG = new Color(50, 50, 50);
    private final Color SIDEBAR_BG = new Color(40, 40, 40);
    private final Color GREEN_TEXT = new Color(40, 200, 40);
    private final Color LABEL_COLOR = Color.LIGHT_GRAY;
    private final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);

    public HotelReservationGUI()
    {
        hotel = new Hotel();
        createAndShowGUI();
    }

    private void createAndShowGUI()
    {
        JFrame frame = new JFrame("Hotel Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(DARK_BACKGROUND);

        frame.add(createHeaderPanel(), BorderLayout.NORTH);
        frame.add(createCenterPanel(), BorderLayout.CENTER);
        frame.add(createSidebar(), BorderLayout.EAST);
        frame.add(createStatusBar(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createHeaderPanel()
    {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BACKGROUND);

        JLabel title = new JLabel("Hotel Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(GREEN_TEXT);
        title.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filterPanel.setBackground(DARK_BACKGROUND);
        JLabel filterLabel = new JLabel("Filter by Category:");
        filterLabel.setForeground(LABEL_COLOR);
        filterPanel.add(filterLabel);

        JComboBox<String> categoryFilterBox = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        styleComboBox(categoryFilterBox);
        categoryFilterBox.addActionListener(e -> populateRoomGrid((String) categoryFilterBox.getSelectedItem()));
        filterPanel.add(categoryFilterBox);

        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.add(filterPanel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private JScrollPane createCenterPanel()
    {
        roomGridPanel = new JPanel(new GridLayout(0, 5, 10, 10));
        roomGridPanel.setBackground(DARK_BACKGROUND);
        roomGridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        populateRoomGrid("All");

        JScrollPane scrollPane = new JScrollPane(roomGridPanel);
        scrollPane.getViewport().setBackground(DARK_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JPanel createSidebar()
    {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, 0));

        actionPanelContainer = new JPanel(new BorderLayout());
        actionPanelContainer.setBackground(SIDEBAR_BG);
        updateActionPanel(null);

        JButton viewAllButton = new JButton("View All Reservations");
        styleButton(viewAllButton);
        viewAllButton.addActionListener(e -> showAllReservationsDialog());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(SIDEBAR_BG);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(viewAllButton);

        sidebar.add(actionPanelContainer, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JLabel createStatusBar()
    {
        statusLabel = new JLabel("Welcome! Select a room to get started.", SwingConstants.CENTER);
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(LABEL_COLOR);
        statusLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        return statusLabel;
    }

    private void populateRoomGrid(String categoryFilter)
    {
        roomGridPanel.removeAll();
        hotel.rooms.stream()
            .filter(room -> categoryFilter.equals("All") || room.category.equals(categoryFilter))
            .forEach(room ->
            {
                RoomPanel roomPanel = new RoomPanel(room);
                roomPanel.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        selectRoom(roomPanel);
                    }
                });
                roomGridPanel.add(roomPanel);
            });
        roomGridPanel.revalidate();
        roomGridPanel.repaint();
    }

    private void selectRoom(RoomPanel panel)
    {
        if (selectedRoomPanel != null)
        {
            selectedRoomPanel.setBorder(selectedRoomPanel.defaultBorder);
        }
        selectedRoomPanel = panel;
        selectedRoomPanel.setBorder(selectedRoomPanel.selectedBorder);
        updateActionPanel(panel.getRoom());
    }

    private void updateActionPanel(Room room)
    {
        actionPanelContainer.removeAll();
        JPanel newActionPanel = createSidebarSection(room);
        actionPanelContainer.add(newActionPanel, BorderLayout.NORTH);
        actionPanelContainer.revalidate();
        actionPanelContainer.repaint();
    }

    private JPanel createSidebarSection(Room room)
    {
        if (room == null)
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(SIDEBAR_BG);
            JLabel message = new JLabel("<html><center>Select a room to view details and available actions.</center></html>", SwingConstants.CENTER);
            message.setForeground(LABEL_COLOR);
            panel.add(message, BorderLayout.CENTER);
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            return panel;
        }

        String title = room.isBooked ? "Manage Room " + room.roomNumber : "Book Room " + room.roomNumber;
        JPanel panel = createTitledPanel(title);
        GridBagConstraints gbc = createGbc();

        if (room.isBooked)
        {
            String guestName = hotel.getReservationByRoomNumber(room.roomNumber).map(r -> r.name).orElse("N/A");
            addComponent(panel, new JLabel("Status: Booked"), gbc, 0);
            addComponent(panel, new JLabel("Guest: " + guestName), gbc, 1);
            JButton cancelButton = new JButton("Cancel This Reservation");
            styleButton(cancelButton, GREEN_TEXT, new Color(150, 40, 40));
            addComponent(panel, cancelButton, gbc, 2);
            cancelButton.addActionListener(e -> handleCancellation(room));
        }
        else
        {
            addComponent(panel, new JLabel("Guest Name:"), gbc, 0);
            JTextField nameField = new JTextField();
            styleTextField(nameField);
            addComponent(panel, nameField, gbc, 1);
            JButton bookButton = new JButton("Confirm Booking");
            styleButton(bookButton);
            addComponent(panel, bookButton, gbc, 2);
            bookButton.addActionListener(e -> handleBooking(nameField, room));
        }
        return panel;
    }

    private void handleBooking(JTextField nameField, Room room)
    {
        String name = nameField.getText().trim();
        if (name.isEmpty())
        {
            updateStatus("Please enter a guest name.", true);
            return;
        }
        String result = hotel.makeReservation(name, room.roomNumber);
        updateStatus(result, result.startsWith("Error"));
        refreshRoomPanels();
        updateActionPanel(room);
    }

    private void handleCancellation(Room room)
    {
        String guestName = hotel.getReservationByRoomNumber(room.roomNumber).map(r -> r.name).orElse("Unknown Guest");
        int choice = showConfirmationDialog(
            "Are you sure you want to cancel the reservation for " + guestName + " in Room " + room.roomNumber + "?",
            "Confirm Cancellation");

        if (choice == JOptionPane.YES_OPTION)
        {
            String result = hotel.cancelReservation(room.roomNumber);
            updateStatus(result, result.startsWith("Error"));
            refreshRoomPanels();
            updateActionPanel(room);
        }
    }

    private void refreshRoomPanels()
    {
        for (Component c : roomGridPanel.getComponents())
        {
            if (c instanceof RoomPanel)
            {
                ((RoomPanel) c).updateStatus();
            }
        }
    }

    private void updateStatus(String message, boolean isError)
    {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : GREEN_TEXT);
    }

    // --- Component Styling & Helper Methods ---

    private JPanel createTitledPanel(String title)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SIDEBAR_BG);
        Border lineBorder = BorderFactory.createLineBorder(new Color(80, 80, 80));
        panel.setBorder(BorderFactory.createTitledBorder(lineBorder, title,
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, LABEL_FONT, GREEN_TEXT));
        return panel;
    }

    private void addComponent(JPanel panel, JComponent comp, GridBagConstraints gbc, int y)
    {
        if (comp instanceof JLabel)
        {
            comp.setForeground(LABEL_COLOR);
        }
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private GridBagConstraints createGbc()
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        return gbc;
    }

    private void showAllReservationsDialog()
    {
        String[] columnNames = {"Room No.", "Category", "Guest Name", "Payment"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        hotel.reservations.forEach(res -> model.addRow(new Object[]{res.roomNumber, res.category, res.name, res.paymentStatus}));

        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane reportScroll = new JScrollPane(table);
        reportScroll.setPreferredSize(new Dimension(500, 300));

        showStyledDialog(reportScroll, "All Reservations");
    }

    private int showConfirmationDialog(String message, String title)
    {
        UIManager.put("OptionPane.background", SIDEBAR_BG);
        UIManager.put("Panel.background", SIDEBAR_BG);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Button.background", COMPONENT_BG);
        UIManager.put("Button.foreground", GREEN_TEXT);

        return JOptionPane.showConfirmDialog(null, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    private void showStyledDialog(Component content, String title)
    {
        UIManager.put("OptionPane.background", SIDEBAR_BG);
        UIManager.put("Panel.background", SIDEBAR_BG);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        JOptionPane.showMessageDialog(null, content, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void styleButton(JButton button)
    {
        styleButton(button, GREEN_TEXT, COMPONENT_BG);
    }

    private void styleButton(JButton button, Color fg, Color bg)
    {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent evt)
            {
                button.setBackground(bg.brighter());
            }

            @Override
            public void mouseExited(MouseEvent evt)
            {
                button.setBackground(bg);
            }
        });
    }

    private void styleTextField(JTextField field)
    {
        field.setBackground(COMPONENT_BG);
        field.setForeground(GREEN_TEXT);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            new EmptyBorder(5, 5, 5, 5)));
    }

    private void styleComboBox(JComboBox<String> box)
    {
        box.setBackground(COMPONENT_BG);
        box.setForeground(GREEN_TEXT);
        box.setUI(new GreenArrowComboBoxUI());
    }

    private void styleTable(JTable table)
    {
        table.setFont(new Font("Consolas", Font.PLAIN, 14));
        table.setBackground(DARK_BACKGROUND);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 60));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setBackground(COMPONENT_BG);
        header.setForeground(GREEN_TEXT);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setReorderingAllowed(false);
    }

    class GreenArrowComboBoxUI extends BasicComboBoxUI
    {
        @Override
        protected JButton createArrowButton()
        {
            return new BasicArrowButton(BasicArrowButton.SOUTH, COMPONENT_BG, null, GREEN_TEXT, null);
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(HotelReservationGUI::new);
    }
}
