import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class TestFlatLaf {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlatLaf Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.add(new JButton("Nút FlatLaf"));
            frame.setVisible(true);
        });
    }
}
