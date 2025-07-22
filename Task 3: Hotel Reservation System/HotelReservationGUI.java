import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// --- Data Models ---
class Room {
    int roomNumber;
    String category;
    boolean isBooked;

    Room(int roomNumber, String category) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.isBooked = false;
    }
}

class Reservation {
    String name;
    int roomNumber;
    String category;
    String paymentStatus;

    Reservation(String name, int roomNumber, String category, String paymentStatus) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.category = category;
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return String.join(",", name, String.valueOf(roomNumber), category, paymentStatus);
    }

    public static Reservation fromString(String data) {
        String[] parts = data.split(",");
        return new Reservation(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]);
    }
}

// --- Logic Layer ---
class Hotel {
    List<Room> rooms = new ArrayList<>();
    List<Reservation> reservations = new ArrayList<>();
    final String FILE_NAME = "reservations.txt";

    Hotel() {
        initRooms();
        loadReservationsFromFile();
    }

    void initRooms() {
        addRooms(101, 110, "Standard");
        addRooms(201, 205, "Deluxe");
        addRooms(301, 303, "Suite");
    }

    void addRooms(int start, int end, String category) {
        for (int i = start; i <= end; i++) rooms.add(new Room(i, category));
    }

    void loadReservationsFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().map(Reservation::fromString).forEach(res -> {
                reservations.add(res);
                getRoomByNumber(res.roomNumber).ifPresent(room -> room.isBooked = true);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveReservationsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Reservation r : reservations) {
                bw.write(r.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Optional<Room> getRoomByNumber(int roomNumber) {
        return rooms.stream().filter(r -> r.roomNumber == roomNumber).findFirst();
    }

    Optional<Reservation> getReservationByRoomNumber(int roomNumber) {
        return reservations.stream().filter(r -> r.roomNumber == roomNumber).findFirst();
    }

    String makeReservation(String name, int roomNumber) {
        return getRoomByNumber(roomNumber).map(room -> {
            if (room.isBooked) return "Error: Room " + roomNumber + " is already booked!";
            room.isBooked = true;
            reservations.add(new Reservation(name, room.roomNumber, room.category, "Paid"));
            saveReservationsToFile();
            return "Booking successful! Room " + room.roomNumber + " reserved for " + name + ".";
        }).orElse("Error: Room not found.");
    }

    String cancelReservation(int roomNumber) {
        return getRoomByNumber(roomNumber).filter(room -> room.isBooked).map(room -> {
            room.isBooked = false;
            reservations.removeIf(res -> res.roomNumber == roomNumber);
            saveReservationsToFile();
            return "Reservation for Room " + roomNumber + " has been cancelled.";
        }).orElse("Error: No reservation found for Room " + roomNumber + ".");
    }
}

// GUI Classes remain unchanged
