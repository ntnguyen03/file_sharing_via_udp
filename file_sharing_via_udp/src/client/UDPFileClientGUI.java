package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPFileClientGUI extends JFrame {
    private JTable table;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextField ipField, portField;
    private JButton chooseButton;

    public UDPFileClientGUI() {
        setTitle("UDP File Client");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bảng lịch sử
        table = new JTable(new DefaultTableModel(new Object[]{"Tên file", "Dung lượng", "Người nhận"}, 0));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Thanh trạng thái
        JPanel bottomPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        statusLabel = new JLabel("Chưa gửi file nào");
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Server IP + port
        JPanel topPanel = new JPanel(new FlowLayout());
        ipField = new JTextField("localhost", 15);
        portField = new JTextField("8888", 5);
        chooseButton = new JButton("Chọn file và gửi");

        topPanel.add(new JLabel("Server IP:"));
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(chooseButton);
        add(topPanel, BorderLayout.NORTH);

        chooseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                sendFile(file);
            }
        });
    }

    private void sendFile(File file) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName(ipField.getText());
                int port = Integer.parseInt(portField.getText());

                long fileSize = file.length();

                // Gửi HEADER
                String header = "HEADER|" + file.getName() + "|" + fileSize;
                byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(headerBytes, headerBytes.length, address, port));

                // Gửi DATA
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long sentBytes = 0;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, port);
                        socket.send(packet);
                        sentBytes += bytesRead;

                        int progress = (int) ((sentBytes * 100) / fileSize);

                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                            statusLabel.setText("Đang gửi: " + file.getName());
                        });
                    }
                }

                // Gửi END
                String endMsg = "END|" + file.getName();
                byte[] endBytes = endMsg.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(endBytes, endBytes.length, address, port));

                // Update GUI
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.addRow(new Object[]{file.getName(), file.length() + " bytes", ipField.getText()});
                    statusLabel.setText("Hoàn tất gửi: " + file.getName());
                    progressBar.setValue(100);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPFileClientGUI().setVisible(true));
    }
}
