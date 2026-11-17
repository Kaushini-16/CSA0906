import java.awt.*;
import java.awt.event.*;

class FeeService {
    String payFee(String reg, String text) {
        if (reg == null || reg.trim().isEmpty()) throw new IllegalArgumentException("Register number is required.");
        double amount;
        try { amount = Double.parseDouble(text.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Enter a valid amount."); }
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        if (amount > 100000) throw new IllegalArgumentException("Amount exceeds the limit.");
        return String.format("Payment successful for %s: Rs. %.2f", reg.trim(), amount);
    }
}
public class FeeWindow extends Frame {
    private final TextField reg = new TextField(18), amount = new TextField(18);
    private final Label message = new Label("Enter details and click Pay.");
    private final FeeService service = new FeeService();
    private String receipt = "";
    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new BorderLayout(8,8));
        Panel fields = new Panel(new GridLayout(2,2,8,8));
        fields.add(new Label("Register number:")); fields.add(reg);
        fields.add(new Label("Amount (Rs.):")); fields.add(amount);
        Panel buttons = new Panel();
        Button pay = new Button("Pay"), clear = new Button("Clear"), show = new Button("Receipt");
        buttons.add(pay); buttons.add(clear); buttons.add(show);
        add(message, BorderLayout.NORTH); add(fields, BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
        pay.addActionListener(e -> {
            try { receipt = service.payFee(reg.getText(), amount.getText()); message.setText(receipt); }
            catch (IllegalArgumentException ex) { message.setText("Error: " + ex.getMessage()); }
        });
        clear.addActionListener(e -> { reg.setText(""); amount.setText(""); message.setText("Fields cleared."); });
        show.addActionListener(e -> message.setText(receipt.isEmpty() ? "No payment yet." : receipt));
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); }});
        setSize(430,190); setLocationRelativeTo(null);
    }
    public static void main(String[] args) { EventQueue.invokeLater(() -> new FeeWindow().setVisible(true)); }
}
