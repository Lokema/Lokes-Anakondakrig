import java.awt.EventQueue;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Main extends JFrame {
    public Main() {
        initUI();
    }

    private void initUI() {
        add(new Game());

        setTitle("LOKES ANAKONDAKRIG");
        setSize(820, 600);
        URL iconURL = getClass().getResource("res/icon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Main ex = new Main();
            ex.setVisible(true);
        });
    }
}
