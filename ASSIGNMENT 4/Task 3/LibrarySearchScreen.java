import java.awt.*;
import javax.swing.*;
public class LibrarySearchScreen extends JFrame {
    JTextField query = new JTextField(20);
    JComboBox<String> category = new JComboBox<>(new String[]{"All","Java","Database","Networking"});
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> results = new JList<>(model);
    JLabel status = new JLabel("Ready");
    LibrarySearchScreen() {
        super("Campus Library Search"); setLayout(new BorderLayout(8,8));
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Search:")); search.add(query); search.add(category);
        JButton find = new JButton("Search"); search.add(find);
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Library Book Search",SwingConstants.CENTER),BorderLayout.NORTH);
        top.add(search,BorderLayout.SOUTH);
        JPanel buttons = new JPanel(new FlowLayout());
        JButton prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
        buttons.add(prev); buttons.add(next); buttons.add(reserve);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.add(buttons,BorderLayout.CENTER); bottom.add(status,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH); add(new JScrollPane(results),BorderLayout.CENTER); add(bottom,BorderLayout.SOUTH);
        find.addActionListener(e -> search()); query.addActionListener(e -> search());
        prev.addActionListener(e -> status.setText("Previous page selected."));
        next.addActionListener(e -> status.setText("Next page selected."));
        reserve.addActionListener(e -> status.setText(results.getSelectedValue()==null ? "Select a book first." : "Reservation requested: "+results.getSelectedValue()));
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(650,420); setLocationRelativeTo(null);
    }
    void search() {
        model.clear(); String q=query.getText().trim();
        if(q.isEmpty()){status.setText("Enter a keyword.");return;}
        model.addElement(q+" – Introduction to "+category.getSelectedItem());
        model.addElement(q+" – Practical Guide"); status.setText("2 sample results found.");
    }
    public static void main(String[] a){SwingUtilities.invokeLater(()->new LibrarySearchScreen().setVisible(true));}
}
