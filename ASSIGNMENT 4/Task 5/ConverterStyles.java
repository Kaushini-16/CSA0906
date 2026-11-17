import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class ConverterStyles {
    static void convert(JTextField amount,JComboBox<String> target,JLabel result,JFrame frame) {
        try {
            double v=Double.parseDouble(amount.getText().trim());
            String c=(String)target.getSelectedItem();
            double rate="USD".equals(c)?0.012:0.011;
            result.setText(String.format("%.2f %s",v*rate,c));
        } catch(NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(frame,"Please enter a valid amount.","Invalid amount",JOptionPane.ERROR_MESSAGE);
        }
    }
    static class FrameVersion extends JFrame implements ActionListener {
        JTextField amount=new JTextField(8); JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); JLabel result=new JLabel("Result");
        FrameVersion(){super("Frame implements ActionListener");setLayout(new FlowLayout());JButton b=new JButton("Convert");add(amount);add(target);add(b);add(result);b.addActionListener(this);setDefaultCloseOperation(DISPOSE_ON_CLOSE);pack();}
        public void actionPerformed(ActionEvent e){convert(amount,target,result,this);}
    }
    static class NamedInnerVersion extends JFrame {
        JTextField amount=new JTextField(8); JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); JLabel result=new JLabel("Result");
        NamedInnerVersion(){super("Named Inner Class");setLayout(new FlowLayout());JButton b=new JButton("Convert");add(amount);add(target);add(b);add(result);b.addActionListener(new Handler());setDefaultCloseOperation(DISPOSE_ON_CLOSE);pack();}
        class Handler implements ActionListener {public void actionPerformed(ActionEvent e){convert(amount,target,result,NamedInnerVersion.this);}}
    }
    static class AnonymousVersion extends JFrame {
        JTextField amount=new JTextField(8); JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); JLabel result=new JLabel("Result");
        AnonymousVersion(){super("Anonymous Inner Class");setLayout(new FlowLayout());JButton b=new JButton("Convert");add(amount);add(target);add(b);add(result);b.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){convert(amount,target,result,AnonymousVersion.this);}});setDefaultCloseOperation(DISPOSE_ON_CLOSE);pack();}
    }
    static class LambdaVersion extends JFrame {
        JTextField amount=new JTextField(8); JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); JLabel result=new JLabel("Result");
        LambdaVersion(){super("Currency Converter – Lambda");setLayout(new FlowLayout());JButton b=new JButton("Convert");add(new JLabel("INR"));add(amount);add(target);add(b);add(result);
            // TODO 1: one lambda listener is shared by the button and Enter key.
            ActionListener action=e->convert();
            b.addActionListener(action); amount.addActionListener(action);
            addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){dispose();}});
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);pack();}
        void convert(){ // TODO 2: parse amount and show an error dialog if invalid.
            ConverterStyles.convert(amount,target,result,this);
        }
    }
    public static void main(String[] a){SwingUtilities.invokeLater(()->new LambdaVersion().setVisible(true));}
}
