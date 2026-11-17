import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
public class AppointmentDesk extends JFrame {
    JTextField patient = new JTextField(12);
    JComboBox<String> dept = new JComboBox<>(new String[]{"Cardiology","Dermatology","Orthopedics"});
    JComboBox<String> doctor = new JComboBox<>();
    JComboBox<String> slot = new JComboBox<>(new String[]{"09:00 AM","10:00 AM","11:30 AM","02:00 PM"});
    JLabel status = new JLabel("Enter patient details.");
    boolean bookingInProgress = false;
    Map<String, java.util.List<String>> doctors = Map.of(
      "Cardiology", java.util.List.of("Dr. Arun","Dr. Meena"),
      "Dermatology", java.util.List.of("Dr. Priya","Dr. Karthik"),
      "Orthopedics", java.util.List.of("Dr. Kumar","Dr. Nisha"));
    AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        setLayout(new BorderLayout(8,8));
        JPanel p = new JPanel(new GridLayout(4,2,8,8));
        p.add(new JLabel("Patient ID:")); p.add(patient); p.add(new JLabel("Department:")); p.add(dept);
        p.add(new JLabel("Doctor:")); p.add(doctor); p.add(new JLabel("Time slot:")); p.add(slot);
        JButton book = new JButton("Book Appointment");
        JPanel b = new JPanel(); b.add(book);
        add(status,BorderLayout.NORTH); add(p,BorderLayout.CENTER); add(b,BorderLayout.SOUTH);
        dept.addActionListener(e -> refreshDoctors());
        book.addActionListener(e -> book());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) {
            if (bookingInProgress && JOptionPane.showConfirmDialog(AppointmentDesk.this,
                "Booking in progress. Close?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            dispose();
        }});
        refreshDoctors(); setSize(480,270); setLocationRelativeTo(null);
    }
    void refreshDoctors() {
        doctor.removeAllItems();
        for (String d : doctors.getOrDefault((String)dept.getSelectedItem(), java.util.List.of())) doctor.addItem(d);
    }
    void book() {
        if (patient.getText().trim().isEmpty()) { status.setText("Error: Patient ID is required."); return; }
        bookingInProgress = true;
        try {
            String msg = "Booked for " + patient.getText().trim() + " with " + doctor.getSelectedItem() + " at " + slot.getSelectedItem();
            status.setText(msg); JOptionPane.showMessageDialog(this,msg,"Confirmation",JOptionPane.INFORMATION_MESSAGE);
        } finally { bookingInProgress = false; }
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true)); }
}
