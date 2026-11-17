import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class EventRegistrationApp extends JFrame {
    static final String URL="jdbc:mysql://localhost:3306/campus_events", USER="root", PASSWORD="YOUR_PASSWORD";
    JTextField studentId=new JTextField(9), studentName=new JTextField(10);
    JComboBox<String> event=new JComboBox<>(new String[]{"Cultural Fest","Technical Symposium","Sports Day"});
    JComboBox<String> filter=new JComboBox<>(new String[]{"All","Cultural Fest","Technical Symposium","Sports Day"});
    DefaultTableModel model=new DefaultTableModel(new String[]{"Student ID","Student Name","Event"},0);
    JTable table=new JTable(model); JLabel status=new JLabel("Ready"); JButton register=new JButton("Register");
    EventRegistrationApp(){
        super("Campus Event Registration");setLayout(new BorderLayout(8,8));
        JMenuBar bar=new JMenuBar();JMenu file=new JMenu("File");JMenuItem refresh=new JMenuItem("Refresh"),exit=new JMenuItem("Exit");
        refresh.addActionListener(e->load());exit.addActionListener(e->dispose());file.add(refresh);file.add(exit);bar.add(file);setJMenuBar(bar);
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));form.add(new JLabel("Student ID"));form.add(studentId);form.add(new JLabel("Name"));form.add(studentName);form.add(event);form.add(register);form.add(new JLabel("Filter"));form.add(filter);
        JButton apply=new JButton("Apply");form.add(apply);add(form,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);add(status,BorderLayout.SOUTH);
        register.addActionListener(e->registerStudent());apply.addActionListener(e->load());
        setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(850,450);setLocationRelativeTo(null);load();
    }
    Connection connect() throws SQLException{return DriverManager.getConnection(URL,USER,PASSWORD);}
    void registerStudent(){
        String id=studentId.getText().trim(),name=studentName.getText().trim(),ev=(String)event.getSelectedItem();
        if(id.isEmpty()||name.isEmpty()){status.setText("Student ID and name are required.");return;}
        register.setEnabled(false);
        new SwingWorker<String,Void>(){
            protected String doInBackground(){
                String sql="INSERT INTO registrations(student_id,student_name,event_name) VALUES(?,?,?)";
                try(Connection c=connect();PreparedStatement p=c.prepareStatement(sql)){
                    p.setString(1,id);p.setString(2,name);p.setString(3,ev);p.executeUpdate();return "Registration successful.";
                }catch(SQLIntegrityConstraintViolationException ex){return "Duplicate student/event registration.";}
                catch(SQLException ex){return "Database error: "+ex.getMessage();}
            }
            protected void done(){register.setEnabled(true);try{status.setText(get());}catch(Exception ex){status.setText(ex.getMessage());}load();}
        }.execute();
    }
    void load(){
        model.setRowCount(0);String selected=(String)filter.getSelectedItem();
        new SwingWorker<Void,Object[]>(){
            protected Void doInBackground(){
                boolean all=selected==null||"All".equals(selected);
                String sql="SELECT student_id,student_name,event_name FROM registrations"+(all?"":" WHERE event_name=?")+" ORDER BY student_id";
                try(Connection c=connect();PreparedStatement p=c.prepareStatement(sql)){
                    if(!all)p.setString(1,selected);
                    try(ResultSet r=p.executeQuery()){while(r.next())publish(new Object[]{r.getString(1),r.getString(2),r.getString(3)});}
                }catch(SQLException ex){publish(new Object[]{"","DB error: "+ex.getMessage(),""});}
                return null;
            }
            protected void process(java.util.List<Object[]> rows){for(Object[] row:rows)model.addRow(row);}
            protected void done(){status.setText("Registration list loaded.");}
        }.execute();
    }
    public static void main(String[] a){SwingUtilities.invokeLater(()->new EventRegistrationApp().setVisible(true));}
}
