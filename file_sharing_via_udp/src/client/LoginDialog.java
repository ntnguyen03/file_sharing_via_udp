package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.*;

public class LoginDialog extends JDialog {
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn, registerBtn, guestBtn;
    private boolean succeeded = false;
    private boolean isGuest = false;
    private String loggedInUser = "";

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    // Màu sắc
    private static final Color HEADER_BLUE = new Color(0, 123, 255);
    private static final Color BTN_GREEN = new Color(40, 167, 69);
    private static final Color BTN_ORANGE = new Color(255, 193, 7);
    private static final Color BTN_GRAY = new Color(108, 117, 125);

    public LoginDialog(Frame parent) {
        super(parent, "ĐĂNG NHẬP HỆ THỐNG UDP", true);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BLUE);
        headerPanel.setPreferredSize(new Dimension(400, 80));
        JLabel titleLabel = new JLabel("HỆ THỐNG TRUYỀN FILE QUA UDP", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // FORM
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        userField = new JTextField(15);
        userField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        passField = new JPasswordField(15);
        passField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(passField, gbc);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        guestBtn = new JButton("Dùng không cần Login");
        styleButton(guestBtn, BTN_GRAY);

        registerBtn = new JButton("Đăng ký");
        styleButton(registerBtn, BTN_ORANGE);
        registerBtn.setForeground(Color.BLACK);

        loginBtn = new JButton("Đăng nhập");
        styleButton(loginBtn, BTN_GREEN);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT)); left.setBackground(Color.WHITE); left.add(guestBtn);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setBackground(Color.WHITE); right.add(registerBtn); right.add(loginBtn);

        btnPanel.add(left, BorderLayout.WEST);
        btnPanel.add(right, BorderLayout.EAST);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // Events
        loginBtn.addActionListener(e -> performLogin());
        registerBtn.addActionListener(e -> performRegister());
        guestBtn.addActionListener(e -> {
            isGuest = true; succeeded = true; loggedInUser = "Guest"; dispose();
        });

        getContentPane().add(mainPanel);
        pack(); setSize(550, 300); setLocationRelativeTo(parent); setResizable(false);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12)); btn.setPreferredSize(new Dimension(100, 35));
        if(bg == BTN_GRAY) btn.setPreferredSize(new Dimension(160, 35));
    }

    private void performLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if(user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin"); return; }
        if(sendAuthRequest("LOGIN|" + user + "|" + pass)) {
            succeeded = true; isGuest = false; loggedInUser = user; dispose();
        } else JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void performRegister() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if(user.isEmpty() || pass.isEmpty()) return;
        if(sendAuthRequest("REGISTER|" + user + "|" + pass + "|" + user)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
        } else JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private boolean sendAuthRequest(String msg) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(2000);
            byte[] data = msg.getBytes();
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
            socket.send(packet);
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);
            String res = new String(responsePacket.getData(), 0, responsePacket.getLength());
            return res.equals("LOGIN_OK") || res.equals("REG_OK");
        } catch (Exception e) { return false; }
    }

    public boolean isSucceeded() { return succeeded; }
    public boolean isGuest() { return isGuest; }
    public String getLoggedInUser() { return loggedInUser; }
}